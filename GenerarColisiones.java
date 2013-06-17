/*
 * Clase generadora de Rutas para Instancias de colisión en el problema de rutas con grafo completo.
 * Creación: 25/septiembre/2012.
 * Ultima revisión: 01/noviembre/2012.
 * 
 */
package TestInstancesGenerator;

import Core.TiemposMatrices;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author iooo
 */
public class GenerarColisiones {
    private Random rAleatorio = new Random(); // variable para generación de valores pseudo-aleatorios.
    private int[][] iMatrizConectividad; // almacena la matriz de conectividad del grafo.
    private int[][] iMatrizRutas; // almacena las rutas de la instancia de prueba, cada renglón es una ruta y cada columna es un nodo de la ruta.
    private LinkedList llNodosComunes; // almacena los nodos que se repiten en dos o más de las rutas generadas.
    private LinkedList llDatosNodosComunes = new LinkedList(); // almacena los datos de los nodos que se repiten en dos o más de las rutas generadas (contiene un int[][] en su interior para cada item).
    private int iColisionesFinal; // arreglo auxiliar para la generación de puntos para una ruta.
    private int iDistanciaMaximaAristas = 100; // Distancia máxima que puede tener una arista. Se utiliza en la generación de valores pseudo-aleatorios de la distancia entre nodos.

    public void setllNodosComunes(LinkedList llNodosComunes) {
        this.llNodosComunes = llNodosComunes;
    }
    
    public void setiMatrizRutas(int[][] iMatrizRutas) {
        this.iMatrizRutas = iMatrizRutas;
    }

    public void setLlDatosNodosComunes(LinkedList llDatosNodosComunes) {
        this.llDatosNodosComunes = llDatosNodosComunes;
    }

    public void setSemilla(long lSemilla) {
        this.rAleatorio.setSeed(lSemilla);
    }
    
    public int[][] getiMatrizConectividad() {
        return iMatrizConectividad;
    }

    public int getiColisionesFinal() {
        return iColisionesFinal;
    }
    
    /* 
     * Genera la matriz de conectividad con la mayor cantidad de eventos de colisión posibles.
     * @param iTotalNodosComunes - la máxima cantidad de eventos de colisión que se pueden generar.
     * @param iNodos - la cantidad de nodos que tiene el sistema de navegación.
     */
    public void mGeneraMatrizConectividad (int iTotalNodosComunes, int iNodos){
        // inicializa matriz de conectividad con valores iniciales para todas las conexiones.
        iMatrizConectividad = new int[iNodos][iNodos];
        for (int i = 0; i < iMatrizConectividad.length; i++) {
            for (int j = 0; j < iMatrizConectividad[i].length; j++) {
                if (i==j) {
                    iMatrizConectividad[i][j] = 0; // 0 (es el mismo nodo, no hay conexión).
                }else{
                    iMatrizConectividad[i][j] = -1; // -1 (no hay conexión).
                } // fin if.
            } // fin for j.
        } // fin for i.
        
        // genera tiempos de primera ruta (la ruta con mayor cantidad de nodos comunes).
        int iRutaMasComunes = mEncuentraRutaMasNodosComunes();
        mGeneraTiemposRutaInicial(iRutaMasComunes);

        // genera colisiones mientras existan nodos comunes (se van eliminando de llNodosComunes como se van utilizando).
        int iTotalColisiones = 0;
        while (iTotalColisiones < iTotalNodosComunes && llNodosComunes.size() > 0){
            // generar colisiones en nodos comunes que pertenecen a la ruta con más nodos comunes.
            mGeneraColisionesConRutaMasComunes(iRutaMasComunes);
            // generar colisiones en nodos comunes restantes (los no incluidos en la ruta con más nodos comunes).
            mGeneraColisionesRestantes();
            // calcular el total actual de colisiones en el sistema, para el criterio de parada del proceso.
            iTotalColisiones = mCalculaFitness();
        } // fin while
//        System.out.println("\n Colisiones final: " + iTotalColisiones);
        iColisionesFinal = iTotalColisiones;
    } // fin de método mGeneraiMatrizConectividad.

    
    /*
     *  Calcula el fitness (número de colisiones) actual del sistema.
     * @return -número de colisiones actual del sistema.
     */
    private int mCalculaFitness (){
        // creación de matriz cuadrada con ceros en todos sus elementos, útil solo para llamada al procedimiento de cálculo de fitness.
        int[][] iMatrizTeCalculoFitness = new int[iMatrizRutas.length][iMatrizConectividad.length]; 
        // llamada al procedimiento de cálculo de fitness actual.
        TiemposMatrices tiempos = new TiemposMatrices();
        // inicia matrizTr
        int[][] matrizTr = tiempos.matrizTr(iMatrizTeCalculoFitness, iMatrizRutas);
        int[][] matrizT0 = tiempos.matrizT0(iMatrizConectividad, iMatrizRutas);
        // inicia matrizTt
        int[][] matrizTt = tiempos.matrizTt(matrizT0, iMatrizTeCalculoFitness, matrizTr);
        // calcula fitness 
        int fitness = tiempos.calculaFitness(matrizTt);
        return (fitness);
    } // fin método mCalculaFitness
    
    /* 
     * Encuentra la ruta con la mayor cantidad de nodos comunes.
     * @return iRutaMasComunes - El número (iniciando en 0) de ruta con la mayor cantidad de nodos comunes.
     */
    private int mEncuentraRutaMasNodosComunes () {
        // suma la cantidad de nodos Comunes que tiene cada ruta
        int [] iSumaNodosComunesRutas = new int [iMatrizRutas.length];
        for (int i = 0; i < llNodosComunes.size(); i++) {
            for (int j = 0; j < iMatrizRutas.length; j++) {
                for (int k = 0; k < iMatrizRutas[j].length; k++) {
                    if (llNodosComunes.get(i) == iMatrizRutas[j][k]) {
                        iSumaNodosComunesRutas[j] += 1; // aumenta en uno la cantidad de nodos comunes a la ruta evaluada.
                    } // fin if.
                } // fin for k.
            } // fin for j.
        } // fin for i.
        // checar cual ruta tuvo más nodos Comunes en su trayecto.
        int iRutaMasComunes = 0;
        int iComunes = 0;
        for (int i = 0; i < iSumaNodosComunesRutas.length; i++) {
            if (iComunes < iSumaNodosComunesRutas[i]) {
                iComunes = iSumaNodosComunesRutas[i];
                iRutaMasComunes = i;
            } // fin if.
        } // fin for i.
        return iRutaMasComunes;
    } // fin método mEncuentraRutaMasComunes    
    
    /* 
     * Crea colisiones tomando en cuenta los nodos de la ruta con la mayor cantidad de nodos comunes.
     * @param iRutaMasComunes - El número (iniciando en 0) de ruta con la mayor cantidad de nodos comunes.
     */
    private void mGeneraColisionesConRutaMasComunes (int iRutaMasComunes){
        int iAleatorio, iNodoComun, iPosNodoComun = 0;
        int[][] iDatosComunes = null; // para almacenar temporalmente los datos del nodo común seleccionado.
        // mientras exista al menos un nodo común en el listado que también pertenezca a la ruta con más nodos comunes.
        cicloBuscarComun:
        while (mExistenciaListadoComunesConRutaMasComunes(iRutaMasComunes)) {
            iAleatorio = rAleatorio.nextInt(llNodosComunes.size()); // genera aleatoriamente un número de nodo entre 0 y (cantidad de nodos comunes-1).
            iNodoComun = (int) llNodosComunes.get(iAleatorio); // selecciona un nodo de la lista de nodos comunes usando el valor aleatorio generado.
            // ciclo para checar si iNodoComun es parte de la ruta con más nodos comunes.
            cicloDatosComun:
            for (int j = 0; j < iMatrizRutas[iRutaMasComunes].length; j++) {
                if (iNodoComun == iMatrizRutas[iRutaMasComunes][j]) { 
                    // obtener los datos del nodo seleccionado.
                    llNodosComunes.remove(iAleatorio); // quita el nodo del listado de nodos comunes.
                    iDatosComunes = (int[][]) llDatosNodosComunes.remove(iAleatorio); // obtiene los datos de repetición del nodo seleccionado.
                    iPosNodoComun = j; // posición del nodo común en la ruta con más comunes.
                    break cicloDatosComun; // termina el ciclo de búsqueda de datos del nodo común.
                } // fin if.
            } // fin for j.
            if (iDatosComunes == null) { // si en la asignación del nodo aleatoriamente no se tuvo suerte, regresar a la búsqueda de un nodo común disponible en la ruta con más nodos comunes.
                continue cicloBuscarComun;
            } // fin if.
            // generar tiempos de recorrido con base en un nodo común perteneciente a la ruta con más comunes.
            mGeneraTiemposBaseRutaMasComunes (iRutaMasComunes, iPosNodoComun, iDatosComunes);
            // limpieza del arreglo, para reiniciar la búsqueda de un nodo común pertenenciente a la ruta.
            iDatosComunes = null;
        } // fin while
    } // fin método mColisionesRutaMasComunes
    
    /* 
     * Checa la existencia de al menos un nodo común en el listado de nodos comunes perteneciente a la ruta con la mayor cantidad de nodos comunes.
     * @param iRutaMasComunes - El número (iniciando en 0) de ruta con la mayor cantidad de nodos comunes.
     * @return bExistencia - valor booleano que indica: true-existe al menos un nodo común / false-no existe al menos un nodo común.
     */
    private boolean mExistenciaListadoComunesConRutaMasComunes (int iRutaMasComunes){
        boolean bExistencia = false; // variable para control de la iteración del método.
        // declaración del ciclo que valida la existencia de nodos comunes entre listado de nodos comunes y ruta con más nodos comunes.
        cicloExistencia: 
        for (int i = 0; i < llNodosComunes.size(); i++) {
            for (int j = 0; j < iMatrizRutas[iRutaMasComunes].length; j++) {
                if (llNodosComunes.get(i) == iMatrizRutas[iRutaMasComunes][j]) {
                    bExistencia = true; // encontró al menos un común.
                    break cicloExistencia;
                } // fin if.
            } // fin for j.
        } // fin for i.
        return bExistencia; // retorna verdadero si encontró un nodo común / falso si no encontró un nodo común.
    } // fin método mExistenciaComunes.
    
    /* 
     * Genera los tiempos de ruta en la matriz de conectividad para las rutas que tienen nodos en común con la primera ruta generada.
     * @param iRutaMasComunes - La ruta que tiene más nodos comunes y ya fue generado su recorrido.
     * @param iNodoComun - El nodo común que se está usando para la generación de recorrido.
     * @param iDatosComunes - Arreglo con los datos de las rutas y posiciones del nodo común entre ellas.
     */
    private void mGeneraTiemposBaseRutaMasComunes (int iRutaMasComunes, int iPosNodoComun, int[][] iDatosComunes){
        // calcular el tiempo de recorrido hasta el nodo común sobre la ruta con más comunes.
        int iSumaTiempos = 0; // para almacenar el tiempo de recorrido en la ruta.        
        int iNodoAnterior, iNodoPosterior;
        for (int i = 0; i < iPosNodoComun; i++) {
            iNodoAnterior = iMatrizRutas[iRutaMasComunes][i]-1;
            iNodoPosterior = iMatrizRutas[iRutaMasComunes][i+1]-1;
            iSumaTiempos += iMatrizConectividad[iNodoAnterior][iNodoPosterior];
        } // fin for i.
        
        //generar tiempos de recorrido para las rutas que contienen el nodo común, usando para la generación el tiempo iSumaTiempos.
        int iTiempoGenerado;
        for (int i = 0; i < iDatosComunes.length; i++) {
            iTiempoGenerado = mGeneraTiemposRutaSiguiente(iDatosComunes[i][0], iDatosComunes[i][1], iSumaTiempos); // rutas a partir de 0, posición del nodo a partir de 0.
            /*
            // depuración (darse cuenta cuando hay nodos comunes cruzados en rutas):
            if (iTiempoGenerado != iSumaTiempos) {
                System.out.println("Diferencia en generación de tiempos en las rutas " + (iRutaMasComunes+1) + " y " + (iDatosComunes[i][0]+1) + " con el nodo "+ iNodoComun); // mensaje de error en cálculo.
            } // fin if.
            // fin depuración.
            * 
            */
        } // fin for i.
    } // fin método mGeneraTiemposComunesRutaMasComunes.
    
    /* 
     * Crea colisiones tomando en cuenta la lista de nodos comunes que no pertenecen a la ruta con más nodos comunes.
     */
    private void mGeneraColisionesRestantes (){
        int iAleatorio;
        int iSumaTiempos;
        int[][] iDatosComunes = null; // para almacenar temporalmente los datos del nodo común seleccionado.
        // mientras exista al menos un nodo común en el listado de nodos comunes.
        while (!llNodosComunes.isEmpty()) {            
            iAleatorio = rAleatorio.nextInt(llNodosComunes.size()); // selecciona aleatoriamente un nodo de la lista.
            // obtener los datos del nodo seleccionado del listado de nodos comunes.
            llNodosComunes.remove(iAleatorio); // selecciona una posición del listado de Comunes.
            iDatosComunes = (int[][]) llDatosNodosComunes.remove(iAleatorio); // obtiene los datos de repetición del nodo seleccionado.
            // generar tiempos de recorrido usando el nodo común elegido.
            iSumaTiempos = 0;
            for (int i = 0; i < iDatosComunes.length; i++) {
                iSumaTiempos = mGeneraTiemposRutaSiguiente(iDatosComunes[i][0], iDatosComunes[i][1], iSumaTiempos); // ruta a partir de 0, posición del nodo en la ruta a partir de 0.
            } // fin for i.
        } // fin while
    } // fin método mColisionesRutaMasComunes
    
    /* 
     * Genera los tiempos de ruta en la matriz de conectividad.
     * @param iRutaNodo - La ruta en la que se va a generar tiempos de recorrido hasta la posición del nodo iPosNodo.
     * @param iPosNodo - La posición del nodo común en el arreglo iMatrizRutas hasta donde se van a generar tiempos.
     * @param iSumaTiempos - El tiempo de recorrido que se tiene que generar (si es cero, se desconoce parte o la totalidad de tiempos de la ruta a generar, de lo contrario hay que ajustarse al tiempo especificado).
     * @return int - El tiempo total de recorrido de la ruta generada.
     */
    private int mGeneraTiemposRutaSiguiente (int iRutaNodo, int iPosNodo, int iSumaTiempos){
        int iRecorrido = 0; // almacena el tiempo de recorrido.
        int iDistancia = 0; // para la distancia entre dos nodos.
        int iNodoAnterior, iNodoPosterior;
        if (iSumaTiempos == 0) { // Si se desconocen los tiempos para generar el segmento de la ruta hasta el nodo solicitado.
            for (int i = 0; i < iPosNodo; i++) { // recorre por segmentos (i-->i+1) la ruta.
                iNodoAnterior = iMatrizRutas[iRutaNodo][i]-1;
                iNodoPosterior = iMatrizRutas[iRutaNodo][i+1]-1;
                if (iMatrizConectividad[iNodoAnterior][iNodoPosterior] == -1) { // si no ha sido generado el tiempo de este segmento de la ruta.
                    int iNodosFaltantes = iPosNodo-i-1;
                    iDistancia = rAleatorio.nextInt(iDistanciaMaximaAristas) + iNodosFaltantes; // distancia de recorrido del segmento de ruta aleatoriamente generada (valor mínimo = número de nodos faltantes en la ruta).
                    // Es una matriz simétrica, el cruce entre nodos i,j es igual al cruce entre nodos j,i.
                    iMatrizConectividad[iNodoAnterior][iNodoPosterior] = iDistancia; // asignación de distancia en el nodo correspondiente en la matriz MCC.
                    iMatrizConectividad[iNodoPosterior][iNodoAnterior] = iDistancia; // asignación de distancia en el nodo correspondiente en la matriz MCC.
                    iRecorrido += iDistancia;
                }else{ // si ya hay tiempos llenados por previas rutas.
                    iRecorrido += iMatrizConectividad[iNodoAnterior][iNodoPosterior]; // suma el tiempo ya generado.
                } // fin if.
            } // fin for i.
            return iRecorrido;
        }else{ // Se conocen los tiempos para generar el segmento de la ruta hasta el nodo solicitado.
            for (int i = 0; i < iPosNodo; i++) { // recorre por segmentos (i-->i+1) la ruta.
                iNodoAnterior = iMatrizRutas[iRutaNodo][i]-1;
                iNodoPosterior = iMatrizRutas[iRutaNodo][i+1]-1;
                if (iMatrizConectividad[iNodoAnterior][iNodoPosterior] == -1) { // si no ha sido generado el tiempo de este segmento de la ruta.
                    if (iRecorrido < iSumaTiempos) { // si el tiempo hasta el momento asignado es menor al tiempo que se debe alcanzar.
                        if (i+1 == iPosNodo) { // si es la última distancia por agregar.
                            iDistancia = iSumaTiempos - iRecorrido; //  se genera la colisión con el tiempo restante.
                        }else{
                            int iDistanciaDisponible = iSumaTiempos-iRecorrido;
                            int iNodosFaltantes = iPosNodo-i;
                            iDistancia = rAleatorio.nextInt(iDistanciaDisponible/iNodosFaltantes); // distancia aleatoriamente asignada entre 0 y el tiempo disponible - nodos faltantes hasta el común.
                            if (iDistancia <= 0) {
                                iDistancia = 1; // valor mínimo de la distancia entre nodos.
                            } // fin if.
                        } // fin if.
                    }else{ // genera cualquier distancia pseudo-aleatoriamente, ya no importa su valor.
                        iDistancia = rAleatorio.nextInt(iDistanciaMaximaAristas); // distancia de recorrido del segmento de ruta aleatoriamente generada (valor mínimo = 1).
                        if (iDistancia <= 0) {
                            iDistancia = 1; // valor mínimo de la distancia entre nodos.
                        } // fin if.
                    }// fin if.
                    // Es una matriz simétrica, el cruce entre nodos i,j es igual al cruce entre nodos j,i.
                    iMatrizConectividad[iNodoAnterior][iNodoPosterior] = iDistancia; // asignación de distancia en el nodo correspondiente en la matriz MCC.
                    iMatrizConectividad[iNodoPosterior][iNodoAnterior] = iDistancia; // asignación de distancia en el nodo correspondiente en la matriz MCC.
                    iRecorrido += iDistancia;
                }else{
                    iRecorrido += iMatrizConectividad[iNodoAnterior][iNodoPosterior]; // suma el tiempo ya generado en la generación de tiempos de otra ruta al mismo nodo.
                }// fin if.
            } // fin for i.
            return iSumaTiempos;
        } // fin if.
    } // fin método mGeneraTiemposRuta
    
    /* 
     * Genera tiempo de recorrido de la ruta inicial (no toma en cuenta tiempos de otras rutas pues no existen).
     * @param iRutaNodo - la ruta (a partir de 0) que contiene la mayor cantidad de nodos comunes.
     */
    private void mGeneraTiemposRutaInicial (int iRutaNodo){
        int iDistancia = 0;
        int iNodoAnterior, iNodoPosterior;
        for (int i = 0; i < iMatrizRutas[iRutaNodo].length-1; i++) { // recorre por segmentos (i-->i+1) la ruta.
            iNodoAnterior = iMatrizRutas[iRutaNodo][i]-1;
            iNodoPosterior = iMatrizRutas[iRutaNodo][i+1]-1;
            if (iMatrizConectividad[iNodoAnterior][iNodoPosterior] == -1) { // si no ha sido generado el tiempo de este segmento de la ruta.
                int iNodosFaltantes = iMatrizRutas[iRutaNodo].length-i-1;
                iDistancia = rAleatorio.nextInt(iDistanciaMaximaAristas) + iNodosFaltantes; // distancia de recorrido del segmento de ruta aleatoriamente generada (valor mínimo = número de nodos faltantes).
                if (iDistancia <= 0) {
                    iDistancia = 1; // valor mínimo de la distancia entre nodos.
                } // fin if.
                // Es una matriz simétrica, el cruce entre nodos i,j es igual al cruce entre nodos j,i.
                iMatrizConectividad[iNodoAnterior][iNodoPosterior] = iDistancia; // asignación de distancia en el nodo correspondiente en la matriz MCC.
                iMatrizConectividad[iNodoPosterior][iNodoAnterior] = iDistancia; // asignación de distancia en el nodo correspondiente en la matriz MCC.
            } // fin if.
        } // fin for i.
    } // fin método mGeneraTiemposRutaInicial.
    
} // fin clase GenerarColisiones.
