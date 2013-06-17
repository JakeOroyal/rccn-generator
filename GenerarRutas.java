/*
 * Clase generadora de Rutas para Instancias de colisión en el problema de rutas con grafo completo.
 * Creación: 25/septiembre/2012.
 * Ultima revisión: 26/octubre/2012.
 * 
 */
package TestInstancesGenerator;

import java.awt.HeadlessException;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.JOptionPane;

/**
 *
 * @author iooo
 */
public class GenerarRutas {
    private Random rAleatorio = new Random(); // variable para generación de valores pseudo-aleatorios.
    private int iNodos; // variable para almacenar el número de nodos de la nube de puntos.
    private int iRutas; // variable para almacenar el número de rutas del problema.
    private int[][] iMatrizRutas; // almacena las rutas de la instancia de prueba, cada renglón es una ruta y cada columna es un nodo de la ruta.
    private LinkedList llNodosComunes; // almacena los nodos que se repiten en dos o más de las rutas generadas.
    private LinkedList llDatosNodosComunes = new LinkedList(); // almacena los datos de los nodos que se repiten en dos o más de las rutas generadas (contiene un int[][] en su interior para cada item).
    private int[] iAux; // arreglo auxiliar para la generación de puntos para una ruta.
    private int iAristasTotalesPorNodos; // para almacenar la cantidad de aristas que se pueden utilizar para generar rutas (por restricción de solo una ruta por cada arista).
    private int iAristasTotalesPorRutas; // para almacenar la cantidad de aristas que se han utilizado para saber si se pueden generar más rutas.

    public void setiNodos(int iNodos) {
        this.iNodos = iNodos;
    }

    public void setiRutas(int iRutas) {
        this.iRutas = iRutas;
    }

    public void setSemilla(long lSemilla) {
        this.rAleatorio.setSeed(lSemilla);
    }

    public int[][] getiMatrizRutas() {
        return iMatrizRutas;
    }

    public LinkedList getLlNodosComunes() {
        return llNodosComunes;
    }

    public LinkedList getLlDatosNodosComunes() {
        return llDatosNodosComunes;
    }
    
    /* 
     * Método para generar la matriz de rutas.
     * @param iMinNodosRutas- número mínimo de nodos para generar las rutas.
     * @param iMaxNodosRutas- número máximo de nodos para generar las rutas.
     * @return boolean - true-indica que se generó la matriz de rutas / false-indica que no se generó la matriz de rutas con los parámetros indicados por falta de aristas disponibles.
     */
    public boolean mGeneraMatrizRutas (int iMinNodosRutas, int iMaxNodosRutas){
        mCalculaAristasTotales(); // calcula el total de aristas que se pueden usar para la generación de rutas, para evitar que nunca termine el programa.
        iMatrizRutas = new int[iRutas][]; // declaración e instanciación de matriz de rutas.
        if (iMinNodosRutas == 0 && iMaxNodosRutas == 0) { // el usuario introduce manualmente las rutas.
            for (int i = 0; i < iRutas; i++) {
                mObtieneRutaUsuario(i);
                iMatrizRutas [i] = (int[]) iAux.clone();
            } // fin for i.
        }else{
            int iTamanoRuta=0;
            if (iMaxNodosRutas==0) { // si el usuario seleccionó la misma cantidad de nodos en todas las rutas.
                iTamanoRuta = iMinNodosRutas; // asigna el tamaño de la ruta con exactamente el número de nodos asignado por el usuario.                                            
                if (!mFactibilidadNodosConstante(iTamanoRuta)) { // checar si se pueden crear las rutas solicitadas de acuerdo al número de aristas útiles.
                    return false; // se sale del método indicando que no se puede completar la generación de rutas.
                } // fin if.
                for (int i = 0; i < iRutas; i++) {
                    mGeneraRuta(i,iTamanoRuta);
                } // fin de for i.
            }else{ // si el usuario seleccionó diferente cantidad de nodos en cada una de las rutas.
                for (int i = 0; i < iRutas; i++) {
                    iTamanoRuta = iMinNodosRutas + (int)(rAleatorio.nextDouble() * ((iMaxNodosRutas - iMinNodosRutas) + 1));
                    // calcula que el tamaño de la ruta está en el intervalo asignado por el usuario y cumple con la restricción de aristas útiles.
                    while (!mFactibilidadNodosVariable(iTamanoRuta) && iTamanoRuta >= iMinNodosRutas) {
                        iTamanoRuta --;
                    } // fin while.
                    if (!mFactibilidadNodosVariable(iTamanoRuta) && iTamanoRuta < iMinNodosRutas) { // checar si todavía se pueden crear rutas dada la cantidad de aristas útiles restantes.
                        return false; // se sale del método indicando que no se puede completar la generación de rutas.
                    } // fin if.
                    mGeneraRuta(i,iTamanoRuta);
                } // fin de for i.            
            } //fin de if de cantidad de nodos en rutas.
        } // fin if-else.
        mBuscarNodosCruce(); // llenar la estructura de nodos con cruce de rutas.
        return true;
    } // fin de método mGeneraRutas.
    
    /* 
     * Método para generar la ruta del tamaño indicado.
     * @param iIndice - número de ruta que se está generando del total que se va a generar.
     * @param iTamanoRuta - tamaño (número de nodos) de la ruta que se va a generar.
     */
    private void mGeneraRuta (int iIndice, int iTamanoRuta){
        iAux = new int[iTamanoRuta];
        for (int j = 0; j < iAux.length; j++) { // recorre el tamaño asignado a la ruta.
            if (j==0) { // si es el inicio de la ruta.
                // asigna el nodo al inicio del recorrido de la ruta.
                iAux[j] = rAleatorio.nextInt(iNodos)+1;
            }else{
                // se inserta un nodo si no hay arista ya utilizada.
                iAux[j] = mInsertarNodo(iAux[j-1]);
            } // fin de if inicio de ruta.
        } // fin de for de j.
        iMatrizRutas [iIndice] = (int[]) iAux.clone();
    } // fin método mGeneraRuta
        
    /* 
     * Método para recibir la entrada de usuario para la generación de rutas.
     * @param iValorI - número de ruta que se está generando del total que se va a generar.
     */
    private void mObtieneRutaUsuario (int iValorI){
        try{
            String sCadena = JOptionPane.showInputDialog("Introduzca los nodos de la ruta "+ String.valueOf(iValorI+1) + " separados por comas.", "");
            String[] sSplitCadena = sCadena.split(",");
            iAux = new int[sSplitCadena.length];
            for (int i = 0; i < iAux.length; i++) {
                if (i==0) {
                    iAux[i] = Integer.valueOf(sSplitCadena[i]);
                }else{
                    if (!mRevisaArista(Integer.valueOf(sSplitCadena[i]), iAux[i-1])){ // si la arista ya está siendo utilizada
                        JOptionPane.showMessageDialog(null, "Una conexión entre nodos ya existe, por favor, modifique la ruta.");
                        mObtieneRutaUsuario(iValorI);
                    }else{
                        iAux[i] = Integer.valueOf(sSplitCadena[i]);
                    } // fin de if.
                } // fin if.
            } // fin for i.
        }catch (NumberFormatException | HeadlessException e){
            System.out.println("Excepción en obtención manual de ruta:" + e);
        } // fin de try-catch
    } // fin de método mObtieneRutaUsuario.
    
    /* 
     * Método para generar pseudo-aleatoriamente un número de nodo para insertar en la ruta.
     * @param iNodoVecino - nodo anterior en la ruta, se usa para checar repetición de arista entre nodos.
     * @return iNodoInsertar - el nodo generado que se insertará en la ruta.
     */
    private int mInsertarNodo(int iNodoVecino){
        int iNodoInsertar;
        do{
            iNodoInsertar = rAleatorio.nextInt(iNodos)+1; // genera un número de nodo pseudo-aleatoriamente.
        }while (!mRevisaArista(iNodoVecino,iNodoInsertar)); // mientras no sea posible insertar la transición entre el nodo vecino y el nuevo.
        return iNodoInsertar;
    } // fin de método mInsertarNodo
    
    /* 
     * Método que revisa que el nodo a insertar en la ruta no genera conflicto por arista previamente ocupada.
     * @param iNodoUno - nodo anterior.
     * @param iNodoDos - nodo a insertar.
     * @return boolean - true-indica que se puede insertar / false-indica no se puede insertar.
     */
    private boolean mRevisaArista (int iNodoUno, int iNodoDos){
        // checar que los dos nodos comparados no sean el mismo nodo.
        if (!mAristaMismoNodo(iNodoUno, iNodoDos)) {
            return false;
        } // fin if.
        // checar si la arista ya está ocupada en la ruta en generación.
        if (!mAristaMismaRuta(iNodoUno, iNodoDos)) {
            return false;
        } // fin if.
        // checar si la arista ya está ocupada en otra ruta.
        if (!mAristaOtraRuta(iNodoUno, iNodoDos)) {
            return false;
        } // fin if.
        return true; // la transición entre iNodoUno e iNodoDos no existe en las rutas y se puede insertar en la ruta actual.
    } // fin de método revisaArista
    
    /* 
     * Método que revisa que el nodo a insertar en la ruta no sea el mismo nodo que el anterior.
     * @param iNodoUno - nodo anterior.
     * @param iNodoDos - nodo a insertar.
     * @return boolean - true-indica que son nodos diferentes, se puede insertar / false-indica que es el mismo nodo, no se puede insertar.
     */
    private boolean mAristaMismoNodo (int iNodoUno, int iNodoDos){
        // checar que los dos nodos comparados no sean el mismo nodo.
        if (iNodoUno == iNodoDos) {
            return false; // mismo nodo.
        } else{
            return true;
        } // fin if.
    } // fin método mMismoNodo.
    
    /* 
     * Método que revisa que la arista con los nodos a insertar no está en la misma ruta.
     * @param iNodoUno - nodo anterior.
     * @param iNodoDos - nodo a insertar.
     * @return boolean - true-indica que el nodo no está contenido en la misma ruta, se puede insertar / false-indica que el nodo está contenido en la ruta, no se puede insertar.
     */
    private boolean mAristaMismaRuta (int iNodoUno, int iNodoDos){
        for (int i = 0; i < iAux.length-1; i++) {
            if ((iAux[i]==iNodoUno && iAux[i+1]==iNodoDos) || (iAux[i]==iNodoDos && iAux[i+1]==iNodoUno)) {
                return false; // arista ocupada en la misma ruta.
            } // fin de if
        } // fin de for i
        return true; // no se encontró la arista ocupada en la misma ruta.
    } // fin método mMismaRuta.
    
    /* 
     * Método que revisa que la arista con los nodos a insertar no está en cualquier otra ruta.
     * @param iNodoUno - nodo anterior.
     * @param iNodoDos - nodo a insertar.
     * @return boolean - true-indica que el nodo no está contenido en otra ruta, se puede insertar / false-indica que el nodo está contenido en otra ruta, no se puede insertar.
     */
    private boolean mAristaOtraRuta(int iNodoUno, int iNodoDos){
        int[] iAux2;
        for (int i = 0; i < iRutas; i++) {
            iAux2 = iMatrizRutas[i];
            if (iAux2!=null) {
                for (int j = 0; j < iAux2.length-1; j++) {
                    if ((iAux2[j]==iNodoUno && iAux2[j+1]==iNodoDos) || (iAux2[j]==iNodoDos && iAux2[j+1]==iNodoUno)) {
                        return false; // arista ocupada por otra ruta.
                    } // fin de if
                } // fin de for j
            } // fin if.
        } // fin de for i.
        return true; // no encontró la arista ocupada en cualquier otra ruta.
    } // fin método mAristaOtraRuta.

    /* 
     * Método para generar un listado con los nodos repetidos en la matriz de rutas.
     */
    private void mBuscarNodosCruce (){
        llNodosComunes = new LinkedList(); // lista enlazada para almacenamiento temporal de nodos encontrados comunes a rutas.
        for (int i = 0; i < iMatrizRutas.length-1; i++) { // la última ruta no se recorre porque ya fue comparada con anteriores
            for (int j = 0; j < iMatrizRutas[i].length; j++) {
                if (! llNodosComunes.contains(iMatrizRutas[i][j])) { // si el nodo a evaluar no está en la lista de nodos Comunes.
                    if (mBuscarRepeticionNodo(i, j, iMatrizRutas[i][j])){ // si el nodo evaluado está presente en dos o más rutas.
                        llNodosComunes.add(iMatrizRutas[i][j]); // se agrega el nodo al listado (nodos a partir de 1).
                    } // fin if.
                } // fin if.
            } // fin for de j.
        } // fin for de i.        
    } // fin método mBuscarNodosCruce
    
    /* 
     * Método que verifica si un nodo está repetido en otro lugar de la instancia.
     * @param iI- índice i del recorrido de la matriz de rutas.
     * @param iJ- índice j del recorrido de la matriz de rutas.
     * @param iNodo - nodo que se busca repetido.
     * @return boolean - true-indica que el nodo está repetido / false-indica que no se encontró repetido el nodo.
     */
    private boolean mBuscarRepeticionNodo (int iI, int iJ, int iNodo){
        boolean bEncontrado = false; // por defecto, se marca como no encontrado repetido el nodo a evaluar.
        LinkedList llRutasComunes = new LinkedList();
        LinkedList llPosicionesComunes = new LinkedList();
        llRutasComunes.add(iI); // agregar la ruta del nodo evaluado.
        llPosicionesComunes.add(iJ); // agregar la posición del nodo evaluado.
        
        for (int i = iI+1; i < iMatrizRutas.length; i++) { // ruta siguiente a la que contiene el nodo que se está analizando.
            for (int j = 0; j < iMatrizRutas[i].length; j++) {
                if (iNodo == iMatrizRutas[i][j]) {
                    llRutasComunes.add(i); // agregar la ruta del nodo encontrado repetido.
                    llPosicionesComunes.add(j); // agregar la posición del nodo encontrado repetido (con inicio de ruta en posición 0).
                    bEncontrado = true; // se encontró el nodo evaluado repetido en al menos dos rutas.
                } // fin if.
            } // fin for de j.
        } // fin for de i.
        // si hay repetición de nodos en rutas, agregar datos a LinkedList.
        if (bEncontrado) {
            int[][] iDatosComunes = new int[llRutasComunes.size()][2];
            for (int i = 0; i < llRutasComunes.size(); i++) {
                iDatosComunes[i][0] = (int) llRutasComunes.get(i); // ruta a partir de 0.
                iDatosComunes[i][1] = (int) llPosicionesComunes.get(i); // posición en la ruta, a partir de 0.
            } // fin for i.
            llDatosNodosComunes.add(iDatosComunes.clone());
        } // fin if.
        return bEncontrado;
    } // fin método mBuscarNodosCruce
    
    /* 
     * Método que calcula el número de aristas útiles para generar rutas,
     * de acuerdo con las restricciones del problema 
     * (una arista pertenece a solamente una ruta y tiene solo un sentido de navegación definido).
     */
    private void mCalculaAristasTotales () {
        iAristasTotalesPorNodos = 0;
        for (int i = 1; i < iNodos; i++) {
            iAristasTotalesPorNodos += (i-1);
        } // fin for i.
    } // fin método mCalculaAristasTotales
    
    /* 
     * Método que revisa la factibilidad de construir la instancia con la cantidad de aristas disponibles para las rutas
     * con la misma cantidad de nodos.
     * @param iTamanoRuta - tamaño constante de nodos en las rutas.
     * @return boolean - true-indica que hay cantidad suficiente de aristas para generar la instancia / false-indica que no es posible generar la instancia con los parámetros indicados.
     */
    private boolean mFactibilidadNodosConstante (int iTamanoRuta) {
        iAristasTotalesPorRutas = iRutas*(iTamanoRuta-1); // calcula el número de aristas necesarias para generar todas las rutas solicitadas con las restricciones del problema.
        if (iAristasTotalesPorRutas <= iAristasTotalesPorNodos) {
            return true; // hay suficientes aristas disponibles.
        }else{
            return false; // no hay la cantidad necesaria de aristas para rutas.
        } // fin if.
    } // fin método mFactibilidadNodosConstante

    /* 
     * Método que revisa si la cantidad de aristas disponible alcanza para generar la ruta actual.
     * @param iTamanoRutaSiguiente - tamaño de nodos en la ruta a generar.
     * @return boolean - true-indica que hay cantidad suficiente de aristas para generar la instancia / false-indica que no es posible generar la instancia con los parámetros indicados.
     */
    private boolean mFactibilidadNodosVariable (int iTamanoRutaSiguiente) {
        iAristasTotalesPorRutas = 0;
        int[] iAux2;
        for (int i = 0; i < iRutas; i++) {
            iAux2 = iMatrizRutas[i];
            if (iAux2 != null) {
                iAristasTotalesPorRutas += iAux2.length-1;
            }else{
                break; // termina ciclo for.
            } // fin if.
        } // fin for i.
        iAristasTotalesPorRutas += iTamanoRutaSiguiente-1; // incrementa el número de nodos que se pretenden usar para generar la siguiente ruta.
        if (iAristasTotalesPorRutas < iAristasTotalesPorNodos) {
            return true; // todavía hay suficientes aristas disponibles.
        }else{
            return false; // no hay la cantidad de aristas para rutas necesaria.
        } // fin if.
    } // fin método mFactibilidadNodosVariable

} // fin clase GenerarRutas

