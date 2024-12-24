import java.io.*;

class FileReaderHelper {
    public static final String filename = "C:/Users/tanuj/OneDrive/Desktop/VIT SEM 3/Operating System/OS_Phase1/Osphase1/input1.txt";
    public static BufferedReader br;

    static {
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String readLine() throws IOException {
        return br.readLine();
    }
}

public class temp extends FileReaderHelper {
    public static final int MAX_MEMORY = 100;
    char[][] M = new char[MAX_MEMORY][4];
    char[] IR = new char[4];
    char[] R = new char[4];
    int IC;
    boolean C;
    int SI;
    StringBuilder buffer = new StringBuilder(40);

    // Output file writer
    BufferedWriter outputWriter;

    public void printMemory() {
        System.out.println("Memory:");
        for (int i = 0; i < MAX_MEMORY; i++) {
            for (int j = 0; j < 4; j++)
                System.out.print(M[i][j]);
            System.out.println();
        }
    }

    public void start_exe() {
        IC = 0;
        while (true) {
            for (int i = 0; i < 4; i++)
                IR[i] = M[IC][i];   //storing the instruction in the ir register from memory location m--i
            IC++;

            if (IR[0] == 'G' && IR[1] == 'D') {
                SI = 1;
                System.out.println();
                MOS();
            } else if (IR[0] == 'P' && IR[1] == 'D') {
                SI = 2;
                System.out.println();
                MOS();
            } else if (IR[0] == 'L' && IR[1] == 'R') {
                int i = (IR[2] - 48) * 10 + (IR[3] - 48);
                for (int j = 0; j < 4; j++)
                    R[j] = M[i][j];
                System.out.println("LR, load register");
            } else if (IR[0] == 'S' && IR[1] == 'R') {
                int i = (IR[2] - 48) * 10 + (IR[3] - 48);
                for (int j = 0; j < 4; j++)
                    M[i][j] = R[j];
                System.out.println("SR, store register");
            } else if (IR[0] == 'C' && IR[1] == 'R') {
                int i = (IR[2] - 48) * 10 + (IR[3] - 48);

                int c = 0;
                for(int in = 0;in<4;in++) {

                    if (M[i][in] == R[in]) {    //check if the char is same v = v

                        c++;
                    }
                }
                //if all four chars are same
                if(c == 4)
                        C = true;
                else
                    C = false;
                System.out.println("CR, compare register");
            } else if (IR[0] == 'B' && IR[1] == 'T') {
                if (C) {    // C is toggle register here that store boolean values
                    IC = (IR[2] - 48) * 10 + (IR[3] - 48);
                    System.out.println("BT, branched-true");
                } else
                    System.out.println("BT, not branched-true");
            } else if (IR[0] == 'H') {
                SI = 3;
                System.out.println("H, halt");
                MOS();
                break;
            } else {
                System.out.println("Invalid instruction");
            }
        }
    }

    public void MOS() {
        buffer.setLength(0);
        if (SI == 1) {
            System.out.println("GD, Read from input device");
            try {
                String data = readLine();
                System.out.println("**********Data:" + data);
                buffer.append(data);
                int i = (IR[2] - 48) * 10 + (IR[3] - 48);
                int k = 0;
                for (int j = 0; j < buffer.length() && j < 40; j++) { //max 40 byte means 10 word*4 will be stored
                    M[i][k++] = buffer.charAt(j);
                    if (k == 4) {
                        k = 0;
                        i++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (SI == 2) {
            System.out.println("PD, Write to output device: ");
            int i = (IR[2] - 48) * 10 + (IR[3] - 48);
            String output = "";
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 4; k++) {
                    if (M[i][k] == 'ඞ') {
                        break;
                    }
                    output += M[i][k];
                }
                i++;
            }
            System.out.println(output);
            // Write output to file
            writeOutputToFile(output);
        } else if (SI == 3) {
            System.out.println("Halt");
            System.out.println("Execution ended");
        } else {
            System.out.println("Invalid interrupt");
        }
    }

    // Method to write output to file
    public void writeOutputToFile(String output) {
        try {
            if (outputWriter == null) {
                // Open the output file in append mode
                outputWriter = new BufferedWriter(new FileWriter("output.txt", true));
            }
            outputWriter.write(output);
            outputWriter.newLine();
            outputWriter.flush(); // Flush the data to file immediately
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        System.out.println("Initialising memory and registers.....");
        for (int i = 0; i < MAX_MEMORY; i++) {
            for (int j = 0; j < 4; j++) {
                M[i][j] = 'ඞ';
            }
        }
        for (int i = 0; i < 4; i++) {
            IR[i] = 'ඞ';
            R[i] = 'ඞ';
        }

        System.out.println("Memory and registers initialised");
    }

    public void load() {
        int block_index = 0;
        try {
            String line;
            while ((line = readLine()) != null) {
                System.out.println();
                System.out.println("Line: " + line);

                buffer.append(line);
                System.out.println("Buffer:" + buffer);
                if (buffer.substring(0, 4).equals("$AMJ")) {
                    SI = 1;
                    buffer.setLength(0);
                    init();
                    System.out.println("AMJ, initialised memory and registers");
                } else if (buffer.substring(0, 4).equals("$DTA")) {
                    System.out.println("DTA, started execution");
                    buffer.setLength(0);
                    start_exe();
                } else if (buffer.substring(0, 4).equals("$END")) {
                    SI = 3;
                    buffer.setLength(0);
                    printMemory();
                    System.out.println("END, ended execution");
                    break;
                } else {
                    if (block_index > MAX_MEMORY) {
                        System.out.println("Memory full");
                        break;
                    }
                    int k = 0;
                    for (int i = 0; k < buffer.length(); i++)
                        for (int j = 0; j < 4; j++) {
                            if (buffer.charAt(k) == 'H') {
                                M[i][j] = buffer.charAt(k++);
                                break;
                            } else
                                M[i][j] = buffer.charAt(k++);
                        }
                    buffer.setLength(0);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error reading file");
            e.printStackTrace();
        } finally {
            System.out.println("Memory loaded");
            printMemory();
            closeOutputFile();
        }
    }

    // Close output file when done
    public void closeOutputFile() {
        try {
            if (outputWriter != null) {
                outputWriter.close();
                System.out.println("Output file closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        temp job = new temp();
        job.load();
    }
}
