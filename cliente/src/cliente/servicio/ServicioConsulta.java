package cliente.servicio;

import cliente.comunicacion.ClienteHTTP;
import cliente.comunicacion.ClienteTCP;
import cliente.dto.ErrorDTO;
import cliente.dto.PersonaDTO;
import cliente.util.Json;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Logica de consulta independiente del protocolo: delega en ClienteTCP o
 * ClienteHTTP segun corresponda, deserializa el JSON recibido y construye un
 * resultado equivalente sin importar el mecanismo de transporte usado. La
 * interfaz grafica solo llama a esta clase; nunca abre sockets ni conexiones
 * HTTP directamente.
 */
public final class ServicioConsulta {
    private final ClienteTCP clienteTcp;
    private final ClienteHTTP clienteHttp;

    public ServicioConsulta(ClienteTCP clienteTcp, ClienteHTTP clienteHttp) {
        this.clienteTcp = clienteTcp;
        this.clienteHttp = clienteHttp;
    }

    public ResultadoConsulta consultar(String cedula, Protocolo protocolo) {
        String errorValidacion = validarCedula(cedula);
        if (errorValidacion != null) {
            return ResultadoConsulta.deError(400, errorValidacion);
        }
        try {
            String crudo = protocolo == Protocolo.TCP
                    ? clienteTcp.consultar(cedula.trim())
                    : clienteHttp.consultar(cedula.trim());
            return interpretar(crudo);
        } catch (SocketTimeoutException e) {
            return ResultadoConsulta.deError(0, "Tiempo de espera agotado esperando la respuesta del servidor.");
        } catch (UnknownHostException e) {
            return ResultadoConsulta.deError(0, "No fue posible resolver la direccion del servidor configurada.");
        } catch (ConnectException e) {
            return ResultadoConsulta.deError(0, "El servidor no esta disponible en la direccion y puerto configurados.");
        } catch (IOException e) {
            return ResultadoConsulta.deError(0, "Error de comunicacion con el servidor: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResultadoConsulta.deError(0, "Error inesperado al procesar la respuesta del servidor.");
        }
    }

    /** Valida localmente antes de enviar cualquier solicitud al servidor. Null si es valida. */
    public static String validarCedula(String cedula) {
        if (cedula == null || cedula.isBlank()) {
            return "Debe ingresar un numero de cedula.";
        }
        if (!cedula.trim().matches("\\d{9}")) {
            return "La cedula debe contener exactamente 9 digitos.";
        }
        return null;
    }

    private ResultadoConsulta interpretar(String crudo) {
        Map<String, Object> objeto;
        try {
            objeto = Json.analizarObjeto(crudo);
        } catch (RuntimeException e) {
            return ResultadoConsulta.deError(0, "El servidor respondio con un JSON invalido.");
        }
        if (Boolean.TRUE.equals(objeto.get("error"))) {
            return new ResultadoConsulta(false, null,
                    new ErrorDTO(true, numero(objeto.get("codigo")), texto(objeto.get("mensaje"))));
        }
        PersonaDTO persona = new PersonaDTO(
                texto(objeto.get("cedula")),
                texto(objeto.get("nombre")),
                texto(objeto.get("primerApellido")),
                texto(objeto.get("segundoApellido")),
                texto(objeto.get("codigoElectoral")),
                texto(objeto.get("provincia")),
                texto(objeto.get("canton")),
                texto(objeto.get("distrito")));
        return ResultadoConsulta.deExito(persona);
    }

    private static String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }

    private static int numero(Object valor) {
        return valor instanceof Number numero ? numero.intValue() : 0;
    }
}
