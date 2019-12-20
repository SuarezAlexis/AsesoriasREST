package mx.unam.dgtic.asesorias;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;

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
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mx.unam.dgtic.asesorias.util.Bundler;
import mx.unam.dgtic.modelo.dto.UsuarioDto;

public class MainActivity extends AppCompatActivity implements Response.Listener<String>, Response.ErrorListener {

    Button startButton;
    ProgressBar progressBar;
    ListView listView;

    RequestQueue queue;
    StringRequest stringRequest;
    ByteArrayInputStream inputStream;

    ArrayList<UsuarioDto> usuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.listView);
        usuarios = new ArrayList<UsuarioDto>();

        progressBar.setVisibility(View.GONE);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                queue = Volley.newRequestQueue(MainActivity.this);
                stringRequest = new StringRequest(
                        Request.Method.GET,
                        getResources().getString(R.string.rest_url),
                        MainActivity.this,
                        MainActivity.this);
                Log.d("DEBUG", "Solicitando por GET desde: " + stringRequest.getUrl());
                queue.add(stringRequest);
            }
        });

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        progressBar.setVisibility(View.GONE);
        Log.d("DEBUG","Error en servicio REST: " + error.getMessage());
        new AlertDialog.Builder(MainActivity.this)
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
            for (int i = 0; i < nList.getLength(); i++) {

                Node nodo = nList.item(i);
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nodo;
                    UsuarioDto u = new UsuarioDto();
                    u.setApellidos(getValue("apellidos",e));
                    u.setEmail(getValue("email",e));
                    u.setHabilitado(Boolean.valueOf(getValue("habilitado",e)));
                    u.setNombre(getValue("nombre",e));
                    u.setPassword(getValue("password",e));
                    u.setUsername(getValue("username",e));
                    usuarios.add(u);
                }
            }

            final UsuariosAdapter adaptador = new UsuariosAdapter(MainActivity.this, usuarios);
            listView.setAdapter(adaptador);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(MainActivity.this, Main2Activity.class);
                    UsuarioDto u = (UsuarioDto)adaptador.getItem(position);
                    i.putExtra("username", u.getUsername());
                    startActivity(i);
                }
            });

        } catch(Exception e) {
            new AlertDialog.Builder(MainActivity.this)
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
