package iezv.jmm.addcopiaagenda;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected List<Contacto> nombres;
    private ListView lv;
    private Button button, button2;
    TextView textView3;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    public static final int REQUEST_CODE = 1;
    boolean memoryStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        textView3 = findViewById(R.id.textView3);
        lv = findViewById(R.id.lvLista);

        memoryStyle = checkTipo();

        cargarContactos();

        buttonHandler();
    }

    public void buttonHandler(){

        //El primer botón se encarga de comparar la Agenda con la Lista actual e importar aquellos
        //contactos que no coincidan en algún atributo.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Contacto> temporal = getListaContactos();
                for (Contacto nombre : temporal){
                    List<String> telfs = getListaTelefonos(nombre.getId());
                    nombre.setTelefono(telfs.get(0));
                }

                for(int i = 0; i<temporal.size(); i++){
                    boolean existe = false;
                    Contacto temp = temporal.get(i);
                    String tempN = temp.getNombre();
                    String tempT = temp.getTelefono();
                    Long tempI = temp.getId();
                    for(int j = 0; j<nombres.size(); j++){
                        Contacto comp = nombres.get(j);
                        String compN = comp.getNombre();
                        String compT = comp.getTelefono();
                        Long compI = comp.getId();
                        if(tempN.equals(compN)&&tempT.equals(compT)&&tempI==compI){
                            existe = true;
                        }
                    }

                    if(!existe){
                        nombres.add(temp);
                    }
                }
                AdaptadorContactoListView aclv = new AdaptadorContactoListView(MainActivity.this, nombres);
                lv.setAdapter(aclv);
            }
        });

        //El segundo botón ofrece la posibilidad de guardar la lista de contactos en memoria interna o externa.
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                elegirGuardado();
                SaveToMemory saver = new SaveToMemory(MainActivity.this, nombres);
                storeTipo(memoryStyle);
            }
        });

        //Es posible editar los Contactos haciendo click sobre ellos.
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Contacto item = (Contacto) lv.getItemAtPosition(i);
                Intent intent = new Intent(MainActivity.this, EditarContacto.class);
                intent.putExtra("contacto", item);
                intent.putExtra("position", i);
                startActivityForResult(intent, REQUEST_CODE);

            }
        });

    }

    //Este método comprueba de dónde debe cargar los Contactos por defecto.
    //En su primera ejecución, los cargará de la Agenda.
    //Si se guarda en memoria interna o externa, la aplicación cargará su propia lista de contactos a partir de entonces.
    private void cargarContactos(){
        if(memoryStyle){
            SaveToMemory saver = new SaveToMemory(this);
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/contactos");
            File file = new File(dir, this.getResources().getString(R.string.externalC));
            if(file == null || !file.exists()){
                leerContactos();
            }else{
                nombres = saver.externalReading();
                AdaptadorContactoListView aclv = new AdaptadorContactoListView(MainActivity.this, nombres);
                lv.setAdapter(aclv);
            }
        }else{
            SaveToMemory saver = new SaveToMemory(this);
            File file = this.getFileStreamPath(getResources().getString(R.string.internalC));
            if(file == null || !file.exists()){
                leerContactos();
            }else{
                nombres = saver.internalReading();
                AdaptadorContactoListView aclv = new AdaptadorContactoListView(MainActivity.this, nombres);
                lv.setAdapter(aclv);
            }
        }
    }

    //Este método ayuda a comprobar si se debe cargar la lista de la memoria interna o externa.
    private boolean checkTipo(){
        SharedPreferences pref = getSharedPreferences(getString(R.string.memorystyle),Context.MODE_PRIVATE);
        boolean v = pref.getBoolean(getString(R.string.tipo),false);
        return v;
    }

    //Esta alerta permitirá al usuario decidir dónde guardar la lista de contactos.
    public void elegirGuardado() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.choicememorytitle));

        builder.setMessage(getResources().getString(R.string.choicememory));
        builder.setPositiveButton(getResources().getString(R.string.choiceext), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                memoryStyle = true;
                permisosEscritura();
                SaveToMemory saver = new SaveToMemory(MainActivity.this, nombres);
                saver.externalWriting();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.choiceint), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                memoryStyle = false;
                SaveToMemory saver = new SaveToMemory(MainActivity.this, nombres);
                saver.internalWriting();
            }
        });
        builder.show();
    }

    public void permisosEscritura(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //textView3.setText(R.string.permissionRequired);
                //button.setEnabled(false);
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    public List<Contacto> getListaContactos(){
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String proyeccion[] = null;
        String seleccion = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = ? and " +
                ContactsContract.Contacts.HAS_PHONE_NUMBER + "= ?";
        String argumentos[] = new String[]{"1","1"};
        String orden = ContactsContract.Contacts.DISPLAY_NAME + " collate localized asc";
        Cursor cursor = getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        int indiceId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int indiceNombre = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        List<Contacto> lista = new ArrayList<>();
        Contacto contacto;
        while(cursor.moveToNext()){
            contacto = new Contacto();
            contacto.setId(cursor.getLong(indiceId));
            contacto.setNombre(cursor.getString(indiceNombre));
            lista.add(contacto);
        }
        return lista;
    }

    public List<String> getListaTelefonos(long id){
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String proyeccion[] = null;
        String seleccion = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String argumentos[] = new String[]{id+""};
        String orden = ContactsContract.CommonDataKinds.Phone.NUMBER;
        Cursor cursor = getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        int indiceNumero = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        List<String> lista = new ArrayList<>();
        String numero;
        while(cursor.moveToNext()){
            numero = cursor.getString(indiceNumero);
            lista.add(numero);
        }
        return lista;
    }

    //Este método almacenará la información sobre si se debe guardar la lista de contactos en memoria interna o externa.
    private void storeTipo(boolean memoryStyle){
        SharedPreferences pref = this.getSharedPreferences(getString(R.string.memorystyle),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(getString(R.string.tipo), memoryStyle);
        editor.commit();
    }

    //Este método cargará los contactos de la Memoria en caso de tener los permisos necesarios.
    //En el caso contrario, inhabilitará el botón que sincroniza la Agenda del teléfono con nuestra lista personalizada.
    public void leerContactos(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                textView3.setText(R.string.permissionRequired);
                button.setEnabled(false);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }else{
            nombres = getListaContactos();
            for (Contacto nombre : nombres){
                List<String> telfs = getListaTelefonos(nombre.getId());
                nombre.setTelefono(telfs.get(0));
                AdaptadorContactoListView aclv = new AdaptadorContactoListView(MainActivity.this, nombres);
                lv.setAdapter(aclv);
            }
        }
    }


    //Este método permitirá mostrar los contactos actualizados si el usuario decide editarlos.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode){
            case REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                    String nombre = data.getStringExtra("nombre");
                    String telefono = data.getStringExtra("telefono");
                    int position = data.getIntExtra("position", -1);

                    if(nombre.equals(getResources().getString(R.string.deleting))){
                        nombres.remove(position);
                        AdaptadorContactoListView aclv = new AdaptadorContactoListView(this, nombres);
                        lv.setAdapter(aclv);
                    }else{
                        nombres.get(position).setNombre(nombre);
                        nombres.get(position).setTelefono(telefono);
                    }

                    break;
                }
        }

    }

    //Este método se encargará de adquirir permisos para leer los Contactos.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission Granted
            nombres = getListaContactos();
            for (Contacto nombre : nombres){
                List<String> telfs = getListaTelefonos(nombre.getId());
                nombre.setTelefono(telfs.get(0));
            }
            AdaptadorContactoListView aclv = new AdaptadorContactoListView(MainActivity.this, nombres);
            lv.setAdapter(aclv);

        }else{
            textView3.setText(R.string.permissionRequired);
            button.setEnabled(false);
        }
    }
}
