package iezv.jmm.addcopiaagenda;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditarContacto extends AppCompatActivity {

    private android.widget.EditText editText;
    private android.widget.EditText editText2;
    private android.widget.Button button3, button4;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_contacto);
        this.button3 = findViewById(R.id.button3);
        this.button4 = findViewById(R.id.button4);
        this.editText2 = findViewById(R.id.editText2);
        this.editText = findViewById(R.id.editText);

        Bundle data = getIntent().getExtras();
        Contacto editC = data.getParcelable("contacto");
        position = data.getInt("position");

        Long id = editC.getId();
        String name = editC.getNombre();
        String phone = editC.getTelefono();

        editText.setText(editC.getNombre());
        editText2.setText(editC.getTelefono());

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditarContacto.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("nombre", editText.getText().toString());
                bundle.putString("telefono", editText2.getText().toString());
                bundle.putInt("position", position);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditarContacto.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("nombre", getResources().getString(R.string.deleting));
                bundle.putString("telefono", "");
                bundle.putInt("position", position);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }


}
