package frontEnd;

public class FrontendSearchResponse {
        private final String cadena;
        private final int cantidad;

        public FrontendSearchResponse(String cadena, int cantidad) {
            this.cadena = cadena;
            this.cantidad = cantidad;
        }

        public String getcadena() {
            return cadena;
        }

        public int getcantidad() {
            return cantidad;
        }
}
