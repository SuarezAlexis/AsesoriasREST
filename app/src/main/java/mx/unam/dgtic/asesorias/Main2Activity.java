package mx.unam.dgtic.asesorias;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mx.unam.dgtic.asesorias.util.Bundler;
import mx.unam.dgtic.modelo.dto.UsuarioDto;

public class Main2Activity extends AppCompatActivity implements Response.Listener<String>, Response.ErrorListener {

    TextView usernameTextView;
    TextView nombreTextView;
    TextView apellidosTextView;
    TextView emailTextView;
    ProgressBar progressBar;

    RequestQueue queue;
    StringRequest stringRequest;
    ByteArrayInputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        usernameTextView = findViewById(R.id.usernameTextView);
        nombreTextView = findViewById(R.id.nombreTextView);
        apellidosTextView = findViewById(R.id.apellidosTextView);
        emailTextView = findViewById(R.id.emailTextView);
        progressBar = findViewById(R.id.progressBar);

        String username = getIntent().getStringExtra("username");

        queue = Volley.newRequestQueue(Main2Activity.this);
        stringRequest = new StringRequest(
                Request.Method.GET,
                getResources().getString(R.string.rest_url) + "/" + username,
                Main2Activity.this,
                Main2Activity.this);
        Log.d("DEBUG", "Solicitando por GET desde: " + stringRequest.getUrl());
        queue.add(stringRequest);

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        progressBar.setVisibility(View.GONE);
        Log.d("DEBUG","Error en servicio REST: " + error.getMessage());
        new AlertDialog.Builder(Main2Activity.this)
                .setTitle(getResources().getString(R.string.alert_title))
                .setMessage(getResources().getString(R.string.alert_message))
                .setPositiveButton(getResources().getString(R.string.alert_positive), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        queue.add(stringRequest);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                queue.add(stringRequest);
            }
        }).setNegativeButton(getResources().getString(R.string.alert_negative), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        })
                .show();
    }

    @Override
    public void onResponse(String response) {
        progressBar.setVisibility(View.GONE);
        inputStream = new ByteArrayInputStream(response.toString().getBytes());
        try {
            DocumentBuilderFactory dbFabrica = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFabrica.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            NodeList nList = doc.getElementsByTagName("usuario");

            Node nodo = nList.item(0);
            if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nodo;
                UsuarioDto u = new UsuarioDto();
                u.setApellidos(getValue("apellidos",e));
                u.setEmail(getValue("email",e));
                u.setHabilitado(Boolean.valueOf(getValue("habilitado",e)));
                u.setNombre(getValue("nombre",e));
                u.setPassword(getValue("password",e));
                u.setUsername(getValue("username",e));

                Toast.makeText(this,"Usuario seleccionado: " + u.getUsername(), Toast.LENGTH_SHORT).show();
                Log.d("DEBUG", "Usuario seleccionado: " + u);

                usernameTextView.setText(u.getUsername());
                nombreTextView.setText(u.getNombre());
                apellidosTextView.setText(u.getApellidos());
                emailTextView.setText(u.getEmail());
            }


        } catch(Exception e) {
            new AlertDialog.Builder(Main2Activity.this)
                    .setTitle(getResources().getString(R.string.alert_title))
                    .setMessage(getResources().getString(R.string.alert_message))
                    .setPositiveButton(getResources().getString(R.string.alert_positive), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            queue.add(stringRequest);
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    queue.add(stringRequest);
                }
            }).setNegativeButton(getResources().getString(R.string.alert_negative), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
                    .show();
        }
    }

    private static String getValue(String tag, Element e) {
        NodeList listaNodos = e.getElementsByTagName(tag).item(0).getChildNodes();
        Node nodo = listaNodos.item(0);
        return nodo.getNodeValue();
    }
}
