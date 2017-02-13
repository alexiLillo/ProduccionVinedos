package cl.lillo.produccionvinedos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cl.lillo.produccionvinedos.Controlador.GestionPesaje;
import cl.lillo.produccionvinedos.Controlador.GestionProducto;
import cl.lillo.produccionvinedos.Controlador.GestionQRSdia;
import cl.lillo.produccionvinedos.Controlador.GestionTablaVista;
import cl.lillo.produccionvinedos.Controlador.GestionTrabajador;
import cl.lillo.produccionvinedos.Controlador.Sync;
import cl.lillo.produccionvinedos.Modelo.Pesaje;
import cl.lillo.produccionvinedos.Otros.CaptureActivityAnyOrientation;
import cl.lillo.produccionvinedos.Otros.QR;

public class MainActivity extends AppCompatActivity {

    NumberFormat formatter = new DecimalFormat("#0.0000");
    NumberFormat formatter2 = new DecimalFormat("#0.0");

    private TextView lastSync, lastSyncCompleta, lastSyncBins, binsDia, cuadrilla, cantidadTrabajadores, qrbin, txtBinsDiaTrabajador, txtBinsMesTrabajador, txtTrabajadorCosulta;
    private ListView listaTrabajadores;

    //spinFundo,
    private Spinner spinPotrero, spinSector, spinVariedad, spinClase, spinEnvase;
    private String scanContent;
    private String scanFormat;

    private String fundo = "1";
    private String potrero = "";
    private String sector = "";
    //private String cuartel = "";
    private String variedad = "";
    private String clase = "";
    private String envase = "";
    private String pesador = "";

    private ArrayList<String> listaTrab = new ArrayList<>();
    private ArrayList<String> listaBins = new ArrayList<>();

    //Gestiones
    private Sync sync;
    private GestionTablaVista gestionTablaVista;
    private GestionPesaje gestionPesaje;
    private GestionTrabajador gestionTrabajador;
    private GestionQRSdia gestionQRSdia;
    private GestionProducto gestionProducto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastSync = (TextView) findViewById(R.id.txtLastSync);
        lastSyncCompleta = (TextView) findViewById(R.id.txtLastSyncCompleta);
        lastSyncBins = (TextView) findViewById(R.id.txtLastSyncBins);
        binsDia = (TextView) findViewById(R.id.txtCountBinsDia);
        qrbin = (TextView) findViewById(R.id.txtQRbin);
        cuadrilla = (TextView) findViewById(R.id.txtCuadrilla);
        cantidadTrabajadores = (TextView) findViewById(R.id.txtCantidadTrabajadores);
        listaTrabajadores = (ListView) findViewById(R.id.listTrabajadores);
        //spinFundo = (Spinner) findViewById(R.id.spinFundo);
        spinPotrero = (Spinner) findViewById(R.id.spinPotrero);
        spinSector = (Spinner) findViewById(R.id.spinSector);
        spinVariedad = (Spinner) findViewById(R.id.spinVariedad);
        spinClase = (Spinner) findViewById(R.id.spinClase);
        spinEnvase = (Spinner) findViewById(R.id.spinEnvase);
        txtBinsDiaTrabajador = (TextView) findViewById(R.id.txtBinsDiaTrabajador);
        txtBinsMesTrabajador = (TextView) findViewById(R.id.txtBinsMesTrabajador);
        txtTrabajadorCosulta = (TextView) findViewById(R.id.txtTrabajadorConsulta);

        gestionTablaVista = new GestionTablaVista(this);
        gestionPesaje = new GestionPesaje(this);
        gestionTrabajador = new GestionTrabajador(this);
        gestionQRSdia = new GestionQRSdia(this);
        gestionProducto = new GestionProducto(this);
        sync = new Sync();

        Bundle bundle = this.getIntent().getExtras();
        pesador = bundle.getString("pesador");

        limpiar();

        //TABS
        Resources res = getResources();

        TabHost tabs = (TabHost) findViewById(R.id.tabHost);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("mitab1");
        spec.setContent(R.id.linearLayout);
        spec.setIndicator("", res.getDrawable(R.drawable.binin));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("mitab2");
        spec.setContent(R.id.linearLayout2);
        spec.setIndicator("", res.getDrawable(R.drawable.card));
        tabs.addTab(spec);
        //deshabilitar un tab (tab de tarjetas)
        tabs.getTabWidget().getChildTabViewAt(1).setEnabled(false);

        spec = tabs.newTabSpec("mitab3");
        spec.setContent(R.id.linearLayout3);
        spec.setIndicator("", res.getDrawable(R.drawable.sync));
        tabs.addTab(spec);

        spec = tabs.newTabSpec("mitab4");
        spec.setContent(R.id.linearLayout4);
        spec.setIndicator("", res.getDrawable(R.drawable.menu));
        tabs.addTab(spec);

        tabs.setCurrentTab(0);
        //fin tabs

        /*
        ArrayAdapter adapterF = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectFundo());
        spinFundo.setAdapter(adapterF);
        spinFundo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinFundo.getItemAtPosition(position).toString().equals("Seleccione...")) {
                    String[] split = spinFundo.getItemAtPosition(position).toString().split("-");
                    fundo = split[1].replace(" ", "");
                    ArrayAdapter adapterP = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectPotrero(fundo));
                    spinPotrero.setAdapter(adapterP);
                } else if (spinFundo.getItemAtPosition(position).toString().equals("Seleccione...")) {
                    fundo = "";
                    potrero = "";
                    sector = "";
                    variedad = "";
                    cuartel = "";
                    ArrayAdapter adapterP = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectPotrero(""));
                    spinPotrero.setAdapter(adapterP);
                    ArrayAdapter adapterS = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectSector("", ""));
                    spinSector.setAdapter(adapterS);
                    ArrayAdapter adapterV = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectVariedad("", "", ""));
                    spinVariedad.setAdapter(adapterV);
                    //ArrayAdapter adapterC = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectCuartel("", "", "", ""));
                    //spinCuartel.setAdapter(adapterC);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        */

        ArrayAdapter adapterP = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectPotrero(fundo));
        spinPotrero.setAdapter(adapterP);
        spinPotrero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinPotrero.getItemAtPosition(position).toString().equals("Seleccione...")) {
                    String[] split = spinPotrero.getItemAtPosition(position).toString().split("-");
                    potrero = split[0].replace(" ", "");
                    ArrayAdapter adapterS = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectSector(fundo, potrero));
                    spinSector.setAdapter(adapterS);
                } else if (spinPotrero.getItemAtPosition(position).toString().equals("Seleccione...")) {
                    potrero = "";
                    sector = "";
                    variedad = "";
                    clase = "";
                    envase = "";
                    ArrayAdapter adapterS = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectSector("", ""));
                    spinSector.setAdapter(adapterS);
                    ArrayAdapter adapterV = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectVariedad("", "", ""));
                    spinVariedad.setAdapter(adapterV);
                    //ArrayAdapter adapterC = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectCuartel("", "", "", ""));
                    //spinCuartel.setAdapter(adapterC);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinSector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinSector.getItemAtPosition(position).toString().equals("Seleccione...")) {
                    String[] split = spinSector.getItemAtPosition(position).toString().split("-");
                    sector = split[0].replace(" ", "");
                    ArrayAdapter adapterV = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectVariedad(fundo, potrero, sector));
                    spinVariedad.setAdapter(adapterV);
                } else if (spinSector.getItemAtPosition(position).toString().equals("Seleccione...")) {
                    sector = "";
                    variedad = "";
                    clase = "";
                    envase = "";
                    ArrayAdapter adapterV = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectVariedad("", "", ""));
                    spinVariedad.setAdapter(adapterV);
                    //ArrayAdapter adapterC = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectCuartel("", "", "", ""));
                    //spinCuartel.setAdapter(adapterC);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //SPIN CUARTEL no existe, CARGA AHORA LAS CLASES
        spinVariedad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinVariedad.getItemAtPosition(position).toString().equals("Seleccione...")) {
                    String[] split = spinVariedad.getItemAtPosition(position).toString().split(" -");
                    variedad = split[1].replace(" ", "");
                    //ArrayAdapter adapterC = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectCuartel(fundo, potrero, sector, variedad));
                    //spinCuartel.setAdapter(adapterC);
                    ArrayAdapter adapterE = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectEnvases(variedad, clase));
                    spinEnvase.setAdapter(adapterE);
                } else if (spinVariedad.getItemAtPosition(position).toString().equals("Seleccione...")) {
                    variedad = "";
                    clase = "";
                    envase = "";
                    spinClase.setSelection(0,true);
                    //ArrayAdapter adapterC = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectCuartel("", "", "", ""));
                    //spinCuartel.setAdapter(adapterC);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //cargar spin con clases
        ArrayAdapter adapterC = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectClases());
        spinClase.setAdapter(adapterC);
        spinClase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!spinClase.getItemAtPosition(position).toString().equals("Seleccione...")) {
                    clase = spinClase.getItemAtPosition(position).toString().replace(" ", "");
                    if (!variedad.equals("")) {
                        ArrayAdapter adapterE = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectEnvases(variedad, clase));
                        spinEnvase.setAdapter(adapterE);
                    }
                    else {
                        envase = "";
                    }
                } else {
                    clase = "";
                    envase = "";
                    ArrayAdapter adapterE = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectEnvases(variedad, clase));
                    spinEnvase.setAdapter(adapterE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinEnvase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                envase = spinEnvase.getItemAtPosition(position).toString().replace(" ", "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listaTrabajadores.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                if (listaTrab.size() > 0) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Eliminar trabajador")
                            .setMessage("¿Desea quitar de la lista al trabajador:\n" + listaTrabajadores.getItemAtPosition(position) + "?")
                            .setPositiveButton("ELIMINAR", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    listaTrab.remove(position);
                                    if (listaTrab.size() > 0) {
                                        ArrayAdapter adapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_list_item_1, listaTrab);
                                        listaTrabajadores.setAdapter(adapter);
                                        cantidadTrabajadores.setText(String.valueOf(listaTrab.size()));
                                    } else {
                                        ArrayList<String> listaVacia = new ArrayList<>();
                                        listaVacia.add("Ningún trabajador escaneado...");
                                        ArrayAdapter adapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_list_item_1, listaVacia);
                                        listaTrabajadores.setAdapter(adapter);
                                        cantidadTrabajadores.setText("S/D");
                                    }
                                }
                            })
                            .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //NADA QUE HACER
                                }
                            }).show();
                }
                return true;
            }
        });
    }

    public void registrarBin(final ArrayList<String> qrBin) {
        //&& !cuartel.equals("")
        if (!fundo.equals("") && !potrero.equals("") && !sector.equals("") && !variedad.equals("") && !clase.equals("") && !envase.equals("") && !qrbin.getText().toString().equals("S/D") && !cantidadTrabajadores.getText().toString().equals("S/D") && !cuadrilla.getText().toString().equals("S/D")) {
            final Pesaje pesaje = new Pesaje();
            pesaje.setProducto("33");
            //pesaje.setQRenvase(qrBin);
            //pesaje.setRutTrabajador(txtTrabajador.getText().toString());
            //rut pesador se debe escanear al iniciar
            pesaje.setCuadrilla((String) cuadrilla.getText());
            pesaje.setRutPesador(pesador);
            pesaje.setFundo(fundo);
            pesaje.setPotrero(potrero);
            pesaje.setSector(sector);
            pesaje.setVariedad(variedad);
            pesaje.setClase(clase);
            //pesaje.setCuartel(cuartel);
            pesaje.setCuartel("C01");

            Calendar c = Calendar.getInstance();
            int day = c.get(Calendar.DAY_OF_MONTH);
            String dia = "" + day;
            int month = c.get(Calendar.MONTH) + 1;
            String mes = "" + month;
            int year = c.get(Calendar.YEAR);

            if (day < 10)
                dia = "0" + day;
            if (month < 10)
                mes = "0" + mes;
            String fecha = dia + "/" + mes + "/" + year;
            int hour = c.get(Calendar.HOUR_OF_DAY);
            String hora = "" + hour;
            int min = c.get(Calendar.MINUTE);
            String minu = "" + min;
            if (hour < 10)
                hora = "0" + hour;
            if (min < 10)
                minu = "0" + min;
            String horario = hora + ":" + minu;

            pesaje.setFechaHora(fecha + " " + horario);
            // PESO NETO FIJO TRAIDO DE PRODUCTO, SIN TARA
            double cantTrabajadores = Double.parseDouble(String.valueOf(cantidadTrabajadores.getText()));

            //rescatar nombre variedad
            //String cadena[] = spinVariedad.getSelectedItem().toString().split(" -");
            //String nombreVariedad = cadena[0];

            double pesoNeto = gestionTablaVista.getPesoNeto(variedad, clase, envase);

            //String envase = gestionTablaVista.getTipoEnvase();

            pesaje.setPesoNeto(pesoNeto / cantTrabajadores);
            pesaje.setTara(0);
            pesaje.setFormato(envase);
            pesaje.setTotalCantidad(1);
            pesaje.setFactor(cantTrabajadores);
            pesaje.setCantidad(Double.parseDouble(formatter.format((1 / cantTrabajadores))));
            pesaje.setLectura_SVAL("");
            pesaje.setID_Map(gestionTablaVista.lastMapeo());
            pesaje.setTipoRegistro("CELULAR");
            pesaje.setFechaHoraModificacion("-");
            pesaje.setUsuarioModificaion(getImei(getApplicationContext()));

            new AlertDialog.Builder(this)
                    .setTitle("Registrar bins categoría: " + clase)
                    .setCancelable(true)
                    .setMessage("¿Desea registrar: " + qrbin.getText() + "\nperteneciente a los " + cantidadTrabajadores.getText() + " trabajadores\nde la cuadrilla: " + cuadrilla.getText() + "?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            for (String bin : qrBin) {
                                pesaje.setQRenvase(bin);
                                gestionQRSdia.insertarLocal(bin);
                                //REGISTRAR BIN POR CADA TRABAJADOR
                                for (String itemLista : listaTrab) {
                                    String[] cadena = itemLista.split(",");
                                    String rut = cadena[0];
                                    pesaje.setRutTrabajador(rut);
                                    gestionPesaje.insertLocal(pesaje);
                                    gestionPesaje.insertLocalSync(pesaje);
                                }
                            }
                            Toast.makeText(MainActivity.this, "DATOS REGISTRADOS", Toast.LENGTH_SHORT).show();
                            pop();
                            limpiar();
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();

        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Atención!")
                    .setMessage("Complete todos los campos antes de registrar bin")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
    }

    public void insertBin(final View view) {
        registrarBin(listaBins);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null) {
            if (scanningResult.getContents() != null) {
                scanContent = scanningResult.getContents();
                scanFormat = scanningResult.getFormatName();
            }

            QR qr = QR.getInstance();

            if (scanContent != null) {
                if (qr.getTipoQR().equals("registrar")) {
                    if (qrbin.getText().equals("S/D")) {
                        //VALIDAR QR BIN
                        if (!qrbin.getText().equals(scanContent) && !gestionQRSdia.existeLocal(scanContent)) {
                            if (scanContent.startsWith("BIN")) {
                                qrbin.setText(scanContent);
                                listaBins.add(scanContent);
                                ok();
                                scanBin("ESCANEAR QR CUADRILLA");
                            } else {
                                Toast.makeText(this, "CODIGO DE BIN INVALIDO", Toast.LENGTH_SHORT).show();
                                error();
                                scanBin("ESCANEAR QR BIN");
                            }
                        } else {
                            Toast.makeText(this, "CODIGO DE BIN YA LEIDO", Toast.LENGTH_SHORT).show();
                            error();
                            scanBin("ESCANEAR QR BIN");
                        }
                    } else if (cuadrilla.getText().equals("S/D")) {
                        if (!cuadrilla.getText().equals(scanContent)) {
                            if (scanContent.startsWith("CUAD")) {
                                cuadrilla.setText(scanContent);
                                ok();
                                scanBin("ESCANEAR QR TRABAJADOR " + String.valueOf(listaTrab.size() + 1));
                            } else {
                                Toast.makeText(this, "CODIGO DE CUADRILLA INVALIDO", Toast.LENGTH_SHORT).show();
                                error();
                                scanBin("ESCANEAR QR CUADRILLA");
                            }
                        } else {
                            Toast.makeText(this, "CODIGO DE CUADRILLA YA LEIDO", Toast.LENGTH_SHORT).show();
                            error();
                            scanBin("ESCANEAR QR CUADRILLA");
                        }
                    } else {
                        if (gestionTrabajador.existe(scanContent)) {
                            boolean escaneado = false;
                            for (String listado : listaTrab) {
                                if (listado.contains(scanContent)) {
                                    escaneado = true;
                                    break;
                                }
                            }
                            if (!escaneado) {
                                listaTrab.add(scanContent + ",\t\t\t" + gestionTrabajador.getNombre(scanContent));
                                ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listaTrab);
                                listaTrabajadores.setAdapter(adapter);
                                cantidadTrabajadores.setText(String.valueOf(listaTrab.size()));
                                ok();
                                scanBin("ESCANEAR QR TRABAJADOR " + String.valueOf(listaTrab.size() + 1));
                            } else {
                                Toast.makeText(this, "CODIGO DE TRABAJADOR YA LEIDO", Toast.LENGTH_SHORT).show();
                                error();
                                scanBin("ESCANEAR QR TRABAJADOR " + String.valueOf(listaTrab.size() + 1));
                            }
                        } else {
                            Toast.makeText(this, "CODIGO DE TRABAJADOR INVALIDO", Toast.LENGTH_SHORT).show();
                            error();
                            scanBin("ESCANEAR QR TRABAJADOR " + String.valueOf(listaTrab.size() + 1));
                        }
                    }
                } else if (qr.getTipoQR().equals("consultar")) {
                    if (gestionTrabajador.existe(scanContent)) {
                        txtTrabajadorCosulta.setText(gestionTrabajador.getNombre(scanContent));
                        String dia = formatter2.format(gestionPesaje.binsDiaTrabajador(scanContent));
                        String mes = formatter2.format(gestionPesaje.binsMesTrabajador(scanContent));
                        txtBinsDiaTrabajador.setText(dia);
                        txtBinsMesTrabajador.setText(mes);
                        pop();
                    }else {
                        Toast.makeText(this, "CODIGO DE TRABAJADOR INVALIDO", Toast.LENGTH_SHORT).show();
                        error();
                        qrIntent("consultar", "CONSULTAR POR QR DE TRABAJADOR");
                    }
                } else if (qr.getTipoQR().equals("addbins")) {
                    if (!qrbin.getText().equals(scanContent) && !gestionQRSdia.existeLocal(scanContent)) {
                        if (scanContent.startsWith("BIN")) {
                            boolean noescaneado = true;
                            for (String qrbin : listaBins) {
                                if (qrbin.equals(scanContent)) {
                                    noescaneado = false;
                                    break;
                                }
                            }
                            if (noescaneado) {
                                listaBins.add(scanContent);
                                ok();
                                qrbin.setText(listaBins.size() + " bins");
                            } else {
                                Toast.makeText(this, "CODIGO DE BIN YA LEIDO", Toast.LENGTH_SHORT).show();
                                error();
                                qrIntent("addbins", "ESCANEAR OTRO BIN");
                            }
                        } else {
                            Toast.makeText(this, "CODIGO DE BIN INVALIDO", Toast.LENGTH_SHORT).show();
                            error();
                            qrIntent("addbins", "ESCANEAR OTRO BIN");
                        }
                    } else {
                        Toast.makeText(this, "CODIGO DE BIN YA LEIDO", Toast.LENGTH_SHORT).show();
                        error();
                        qrIntent("addbins", "ESCANEAR OTRO BIN");
                    }
                }
            } else {
                //Toast.makeText(this, "No se escanearon datos", Toast.LENGTH_SHORT).show();
            }
            scanContent = null;
        } else {
            Toast.makeText(this, "No se escanearon datos", Toast.LENGTH_SHORT).show();
        }
    }

    public void addBin(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Agregar bin")
                .setCancelable(true)
                .setMessage("¿Desea agregar otro bin a este registro?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        qrIntent("addbins", "ESCANEAR OTRO BIN");
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    public void scanButton(View view) {
        if (qrbin.getText().equals("S/D"))
            scanBin("ESCANEAR QR BIN");
        else if (cuadrilla.getText().equals("S/D"))
            scanBin("ESCANEAR QR CUADRILLA");
        else
            scanBin("ESCANEAR QR TRABAJADOR " + String.valueOf(listaTrab.size() + 1));
    }

    public void scanBin(String title) {
        qrIntent("registrar", title.toUpperCase());
    }

    public void scanConsulta(View view) {
        qrIntent("consultar", "CONSULTAR POR QR DE TRABAJADOR");
    }

    public void qrIntent(String tipo, String titulo) {
        QR qr = QR.getInstance();
        qr.setTipoQR(tipo);
        IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
        scanIntegrator.setPrompt(titulo);
        scanIntegrator.setBeepEnabled(false);
        scanIntegrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.setBarcodeImageEnabled(true);
        scanIntegrator.initiateScan();
    }

    private void pop() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.pop);
        mp.setVolume(50, 50);
        mp.start();
    }

    private void ok() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.ok);
        mp.setVolume(50, 50);
        mp.start();
    }

    private void error() {
        MediaPlayer mp = MediaPlayer.create(this, R.raw.error);
        mp.setVolume(50, 50);
        mp.start();
    }

    public void clear(View view) {
        if (!qrbin.getText().equals("S/D") || !cuadrilla.getText().equals("S/D") || !cantidadTrabajadores.getText().equals("S/D")) {
            new AlertDialog.Builder(this)
                    .setTitle("Datos pendientes")
                    .setMessage("¿Está seguro de limpiar datos que aún no se registran en el sistema?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            limpiar();
                        }
                    })
                    .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        } else {
            limpiar();
        }
    }

    public void limpiar() {
        qrbin.setText("S/D");
        cuadrilla.setText("S/D");
        cantidadTrabajadores.setText("S/D");

        listaTrab.clear();
        ArrayList<String> listaVacia = new ArrayList<>();
        listaVacia.add("Ningún trabajador escaneado...");
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listaVacia);
        listaTrabajadores.setAdapter(adapter);
        binsDia.setText(String.valueOf(gestionPesaje.cantBins(pesador)));

        listaBins.clear();

        txtTrabajadorCosulta.setText("S/D");
        txtBinsDiaTrabajador.setText("S/D");
        txtBinsMesTrabajador.setText("S/D");

        spinClase.setSelection(0,true);
    }


    //SINCRONIZACION
    public void syncCompleta(View view) {
        if (sync.eventoSyncAll(view.getContext(), true)) {
            //lastSync.setText(getHoraActual());
            lastSyncCompleta.setText(getHoraActual());

            //ArrayAdapter adapterF = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectFundo());
            //spinFundo.setAdapter(adapterF);
            ArrayAdapter adapterP = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_dropdown_item, gestionTablaVista.selectPotrero(fundo));
            spinPotrero.setAdapter(adapterP);
        }
    }

    public void syncBins(View view) {
        if (sync.eventoSyncPesaje(view.getContext(), false)) {
            lastSync.setText(getHoraActual());
            lastSyncBins.setText(getHoraActual());
        }
    }

    //HORA ACTUAL
    public String getHoraActual() {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        String dia = "" + day;
        int month = c.get(Calendar.MONTH) + 1;
        String mes = "" + month;
        int year = c.get(Calendar.YEAR);
        if (day < 10)
            dia = "0" + day;
        if (month < 10)
            mes = "0" + mes;
        String fecha = dia + "/" + mes + "/" + year;
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String hora = "" + hour;
        int min = c.get(Calendar.MINUTE);
        String minu = "" + min;
        if (hour < 10)
            hora = "0" + hour;
        if (min < 10)
            minu = "0" + min;

        return hora + ":" + minu;
    }

    //doble back para salir
    private static final int INTERVALO = 2000; //2 segundos para salir
    private long tiempoPrimerClick;

    @Override
    public void onBackPressed() {
        if (tiempoPrimerClick + INTERVALO > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(this, "Vuelva a presionar para salir", Toast.LENGTH_SHORT).show();
        }
        tiempoPrimerClick = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //sync.eventoSyncPesaje(MainActivity.this, false);
    }

    public static String getImei(Context c) {
        TelephonyManager telephonyManager = (TelephonyManager) c
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
