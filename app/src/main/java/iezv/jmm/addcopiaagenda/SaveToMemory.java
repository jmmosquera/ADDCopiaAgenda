package iezv.jmm.addcopiaagenda;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SaveToMemory {

    private Context activityC;
    private String listaContactos;

    public SaveToMemory(){}

    public SaveToMemory(Context activityC){
        this.activityC = activityC;
    }

    public SaveToMemory(Context activityC, List<Contacto> nombres){
        this.activityC = activityC;
        this.listaContactos = listaToCsv(nombres);
    }

    public Context getActivityC() {
        return activityC;
    }

    public void setActivityC(Context activityC) {
        this.activityC = activityC;
    }

    public void internalWriting(){
        String file = activityC.getResources().getString(R.string.internalC);
        String fileContents = listaContactos;
        FileOutputStream outputStream;
        try {
            outputStream = activityC.openFileOutput(file, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void externalWriting(){
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/Android/data");
        dir.mkdir();
        File file = new File(dir, activityC.getResources().getString(R.string.externalC));
        String fileContents = listaContactos;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.println(listaContactos);
            pw.flush();
            pw.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<Contacto> internalReading(){
        List<Contacto> nombres = new ArrayList<Contacto>();
        try {
            FileInputStream in = activityC.openFileInput(activityC.getResources().getString(R.string.internalC));
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null){
                Contacto temp = new Contacto();
                line = line.replace("'", "");
                String[] array = line.split(";");
                temp.setId(Long.parseLong(array[0]));
                temp.setNombre(array[1]);
                temp.setTelefono(array[2]);
                nombres.add(temp);
            }
            br.close();
            isr.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nombres;
    }

    public List<Contacto> externalReading(){
        List<Contacto> nombres = new ArrayList<Contacto>();
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/Android/data");
        File file = new File(dir, activityC.getResources().getString(R.string.externalC));
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null){
                if(!line.equals("")) {
                    Contacto temp = new Contacto();
                    line = line.replace("'", "");
                    String[] array = line.split(";");
                    temp.setId(Long.parseLong(array[0]));
                    temp.setNombre(array[1]);
                    temp.setTelefono(array[2]);
                    nombres.add(temp);
                }
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nombres;
    }


    public String listaToCsv(List<Contacto> nombres){
        String contactos = "";
        for (Contacto nombre : nombres){
            contactos += "'"+nombre.getId()+"';'"+nombre.getNombre()+"';'"+nombre.getTelefono()+"';\n";
        }
        return contactos;
    }

}
