package padron.servicio;
public record ResultadoConsulta(int codigo, Object cuerpo) { public boolean exitoso() { return codigo == 200; } }
