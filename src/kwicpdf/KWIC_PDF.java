package kwicpdf;

//Librerias de apache para leer archivos pdf

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class KWIC_PDF {

    //Sección de datos compartidos
    private static char[] lectorPDF;
    private static List<Integer> paginas = new ArrayList<>();
    private static ArrayList<Character> caracteres = new ArrayList<>();
    private static List<String> palabras = new ArrayList<>(); //Contendrá las líneas de entrada

    private static ArrayList<String> inputSentences = new ArrayList<>();//
    private static Map<String, Set<Integer>> map = new LinkedHashMap<>();
    
    //Programa principal desde el cual se mandan llamar las diversas subrutinas
    public static void main(String[] args) throws IOException {
        try {
            //Primera parte
            entradaPDF();//Leemos el pdf
            filtroParaCaracteresEspeciales();  //metodo para ignorar caracteres indeseados
            filtradoDePalabraPorPagina(); //metodo para generar el archivo con la palabra junto a la pagina que aparece
            inputReading(); //Metodo de reuso para leer la salida generada por el filtro de palabras y paginas
            alphabetize(); //Metodo de reuso para acomodar por orden alfabetico las palabras
            agruparPalabras(); //Metodo especial para agrupar cada palabra y las paginas en las que se repite
            writeToOutput(); //Metodo de reuso para generar archivo de salida para mostrar las palabras como indice

            //Segunda parte
            inputReading2(); //Metodo modificado de reuso para leer archivo de texo
            writeToOutput2(); //Método modificado de reuso para generar salida con filtrado de palabras

        } catch (IOException e) {
            System.out.println("No se pudo abrir el archivo");
        }
    }

    private static void entradaPDF() throws IOException {
        String texto = "";
        int contadorDeRenglones = 0;
        String archivo;
        JFileChooser jFC = new JFileChooser();
        jFC.setDialogTitle("KWIC - Seleccione el archivo PDF deseado");
        jFC.setCurrentDirectory(new File("src"));
        int res = jFC.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            archivo = jFC.getSelectedFile().getPath();
        } else {
            archivo = "src/ejemplo.pdf";
        }
        //Carga de un documento PDF existente
        File file = new File(archivo);
        PDDocument documentoPDF = PDDocument.load(file);

        int numeroDePaginasDelPDF = documentoPDF.getNumberOfPages();
        PDFTextStripper pdfStripper = new PDFTextStripper();

        for (int i = 1; i <= numeroDePaginasDelPDF; i++) {
            pdfStripper.setStartPage(i);
            pdfStripper.setEndPage(i);

            //Recuperando texto de documento PDF
            texto += pdfStripper.getText(documentoPDF);
            texto += "----------------------------\n";

            for (int j = 0; j < texto.length(); j++) {
                if (texto.charAt(j) == '\n') {
                    contadorDeRenglones++;
                }
            }
            paginas.add(contadorDeRenglones);
            contadorDeRenglones = 0;
        }
        lectorPDF = texto.toCharArray();

        //Cerrando el documentoPDF
        documentoPDF.close();
    }

    private static void filtroParaCaracteresEspeciales() {
        for (Character caracter : lectorPDF) {
            if (caracter.equals('(') || caracter.equals(')') || caracter.equals('$') ||
                    caracter.equals('&') || caracter.equals('@') || caracter.equals('#') ||
                    caracter.equals('®') || caracter.equals('•') || caracter.equals('—') ||
                    caracter.equals('*') || caracter.equals('‘') || caracter.equals('’') ||
                    caracter.equals('?') || caracter.equals('.') || caracter.equals(':') ||
                    caracter.equals(';') || caracter.equals('¡') || caracter.equals('!') ||
                    caracter.equals('¿') || caracter.equals(',') || caracter.equals('-') ||
                    caracter.equals('+') || caracter.equals('”') || caracter.equals('“') ||
                    caracter.equals('=') || caracter.equals('"') || caracter.equals('{') ||
                    caracter.equals('}') || caracter.equals('0') || caracter.equals('1') ||
                    caracter.equals('2') || caracter.equals('3') || caracter.equals('4') ||
                    caracter.equals('5') || caracter.equals('6') || caracter.equals('7') ||
                    caracter.equals('8') || caracter.equals('9')) {
            } else {
                caracteres.add(caracter);
            }
        }
    }

    private static void filtradoDePalabraPorPagina() throws IOException {
        Writer writer;
        writer = (new FileWriter(new File("src/input2.txt")));
        String palabra = "";
        int cont = 0;
        int pagina = 0;
        for (Character caracter : caracteres) {
            palabra += caracter;
            if (caracter.equals(' ') || caracter.equals('\n') || !caracter.equals(caracter)) {
                if (palabra.trim().length() > 3) {

                    for (int i = 0; i < paginas.size(); i++) {
                        if (cont < paginas.get(i)) {
                            pagina = i + 1;
                            break;
                        }
                    }
                    palabra = palabra.trim();
                    palabra += "," + pagina;
                    if (!palabra.contains("\n")) {
                        palabra += '\n';
                    }
                    writer.write(palabra);
                    cont++;
                }
                palabra = "";
            }
        }
        writer.flush();
    }

    private static void inputReading() {
        String archivo = "src/input2.txt";
        try {
            File file = new File(archivo);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String sentencia = scanner.nextLine();
                if (sentencia.isEmpty()) {
                    break;
                }
                palabras.add(sentencia);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("El archivo no existe");
        }
    }

    //Ordena la lista de sentencias
    private static void alphabetize() {
        Collections.sort(palabras, String.CASE_INSENSITIVE_ORDER);
    }

    private static void agruparPalabras() {
        String[] partes;
        for (String palabra : palabras) {
            partes = palabra.split(",");
            HashSet<Integer> numPag = new HashSet<Integer>();
            numPag.add(Integer.parseInt(partes[1]));
            if(map.containsKey(partes[0])){
                map.get(partes[0]).add(Integer.parseInt(partes[1]));
            }else{
                map.put(partes[0], numPag);
            }
        }
        System.out.println(" ");

    }

    //Envia la lista de sentencias a un archivo de salida
    private static void writeToOutput() {
        FileWriter salida = null;
        String archivo;
        JFileChooser jFC = new JFileChooser();
        jFC.setDialogTitle("KWIC - Seleccione el archivo de salida");
        jFC.setCurrentDirectory(new File("src"));
        int res = jFC.showSaveDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            archivo = jFC.getSelectedFile().getPath();
        } else {
            archivo = "src/output.txt";
        }
        try {
            salida = new FileWriter(archivo);
            PrintWriter bfw = new PrintWriter(salida);

            for (Map.Entry x: map.entrySet()) {
                String llave = (String) x.getKey();
                Set<Integer> set = (Set<Integer>) x.getValue();
                bfw.println(llave +"," +set);


            }
            bfw.close();
            System.out.println("Se ha creado satisfactoriamente el archivo de texto");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Lee un archivo de entrada y almacena las líneas en una Lista
    private static void inputReading2() {
        String archivo;
        JFileChooser jFC = new JFileChooser();
        jFC.setDialogTitle("KWIC - Seleccione el archivo de datos deseado de la segunda parte");
        jFC.setCurrentDirectory(new File("src"));
        int res = jFC.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            archivo = jFC.getSelectedFile().getPath();
        } else {
            archivo = "src/inputFile.txt";
        }
        try {
            File file = new File(archivo);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String sentencia = scanner.nextLine();
                if (sentencia.isEmpty()) {
                    break;
                }
                inputSentences.add(sentencia);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("El archivo no existe");
        }
    }

    private static void writeToOutput2() throws IOException {
        FileWriter salida = null;
        String archivo;
        JFileChooser jFC = new JFileChooser();
        jFC.setDialogTitle("KWIC - Seleccione el archivo de salida de la segunda parte");
        jFC.setCurrentDirectory(new File("src"));
        int res = jFC.showSaveDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            archivo = jFC.getSelectedFile().getPath();
        } else {
            archivo = "src/output2.txt";
        }
        salida = new FileWriter(archivo);
        PrintWriter bfw = new PrintWriter(salida);

        map.forEach((palabra, numeroDePaginaEnLaQueAparece) -> {
            try {

                for (int i = 0; i < inputSentences.size(); i++) {
                    if (inputSentences.get(i).toLowerCase().contains(palabra.toLowerCase())) {
                        bfw.println(palabra + " " + numeroDePaginaEnLaQueAparece);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Se ha creado satisfactoriamente el archivo de texto");
            }
        });
        bfw.close();
    }
}