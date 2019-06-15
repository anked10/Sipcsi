package com.bufeotec.sipcsi.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bufeotec.sipcsi.Adapter.AdaptadorListadoQuejas;
import com.bufeotec.sipcsi.Models.Areas;
import com.bufeotec.sipcsi.Models.Queja;
import com.bufeotec.sipcsi.R;
import com.bufeotec.sipcsi.Util.Preferences;
import com.bufeotec.sipcsi.WebServices.DataConnection;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

import static android.app.Activity.RESULT_OK;
import static com.bufeotec.sipcsi.WebServices.DataConnection.IP;


public class FeedFragment extends Fragment implements View.OnClickListener , SwipeRefreshLayout.OnRefreshListener {


    Context context;
    Activity activity;
    DataConnection dc, dc2;
    AdaptadorListadoQuejas adaptadorListadoQuejas;
    public ArrayList<Queja> arrayqueja;
    RecyclerView rcv_quejas;
    ProgressBar progressBar;
    CardView cdv_mensaje;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView btn_publicar, dialog_publicar_close, nombre_publicacion;
    public static ArrayList<Areas> arrayArea;
    ArrayList<String> arrayarea;
    Spinner spn_areas;
    AppCompatButton bt_publicar;
    ImageView imagen_publicacion, fotocaptada;
    EditText et_post;
    ImageButton bt_photo;
    Preferences pref;

    boolean valorFoto;


    private int REQUEST_CAMERA = 0, SELET_GALERRY = 9;
    public Uri output, resultUriRecortada;
    String userChoosenTask;


    public FeedFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        context = getContext();
        activity = getActivity();
        pref = new Preferences(context);

        rcv_quejas = view.findViewById(R.id.rcv_quejas);
        progressBar = view.findViewById(R.id.progressbar);
        cdv_mensaje = view.findViewById(R.id.cdv_mensaje);
        btn_publicar = view.findViewById(R.id.btn_publicar);

        cdv_mensaje.setVisibility(View.GONE);


        dc = new DataConnection(activity, "listarQuejas", new Queja(pref.getIdUsuarioPref()), false);
        new FeedFragment.GetQueja().execute();


        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.SwipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(this);


        btn_publicar.setOnClickListener(this);
        return view;
    }

    @Override
    public void onRefresh() {

        dc = new DataConnection(activity, "listarQuejas", new Queja(pref.getIdUsuarioPref()), false);
        new FeedFragment.GetQueja().execute();
    }


    public class GetAreas extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            arrayarea = new ArrayList<String>();
            arrayArea = dc2.getListaAreas();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //Toast.makeText(context,"size"+arrayArea.size(),Toast.LENGTH_LONG).show();

            for (Areas obj : arrayArea) {
                arrayarea.add(obj.getArea_nombre());
            }
            //progressBar.setVisibility(ProgressBar.INVISIBLE);
            arrayarea.add(0, "Seleccione");
            ArrayAdapter<String> adapEquipos = new ArrayAdapter<String>(context, R.layout.spiner_item, arrayarea);
            adapEquipos.setDropDownViewResource(R.layout.spiner_dropdown_item);
            spn_areas.setAdapter(adapEquipos);

        }
    }

    public class GetQueja extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            arrayqueja = dc.getListaQuejas();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressBar.setVisibility(ProgressBar.INVISIBLE);

            //linearLayoutManager.setOrientation(linearLayoutManager.VERTICAL);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(linearLayoutManager.VERTICAL);
            //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            rcv_quejas.setLayoutManager(linearLayoutManager);

            adaptadorListadoQuejas = new AdaptadorListadoQuejas(context, arrayqueja, R.layout.rcv_item_list_quejas, new AdaptadorListadoQuejas.OnItemClickListener() {
                @Override
                public void onItemClick(Queja queja, final int position) {

                    //Toast.makeText(getActivity(),"ID "+empresas.getEmpresas_id(), Toast.LENGTH_SHORT).show();

                    /*Intent intent = new Intent(getContext(), DetalleNegocio.class);
                    intent.putExtra("id_empresa",empresas.getEmpresas_id());
                    startActivity(intent);*/

                }
            });
            rcv_quejas.setAdapter(adaptadorListadoQuejas);
            swipeRefreshLayout.setRefreshing(false);

            if (arrayqueja.size() > 0) {
                cdv_mensaje.setVisibility(View.INVISIBLE);
            } else {
                cdv_mensaje.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void onClick(View v) {

        if (v.equals(btn_publicar)) {
            //Intent i  = new Intent(context, Publicar.class);
            //startActivity(i);
            dialogpublicar();
        }

    }



    private void dialogpublicar() {


        final Dialog dialog = new Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED);
        dialog.setContentView(R.layout.dialog_add_post);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog_publicar_close = dialog.findViewById(R.id.dialog_publicar_close);
        imagen_publicacion = dialog.findViewById(R.id.imagen_publicacion);
        nombre_publicacion = dialog.findViewById(R.id.nombre_publicacion);
        et_post = dialog.findViewById(R.id.et_post);
        fotocaptada = dialog.findViewById(R.id.fotocaptada);
        bt_photo = dialog.findViewById(R.id.bt_photo);


        spn_areas = dialog.findViewById(R.id.spn_areas);
        nombre_publicacion.setText(pref.getNombrePref());
        dc2 = new DataConnection(activity, "listarAreas", false);
        new FeedFragment.GetAreas().execute();


        bt_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        dialog_publicar_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        bt_publicar = (AppCompatButton) dialog.findViewById(R.id.bt_publicar);
        et_post.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bt_publicar.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        bt_publicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!(spn_areas.getSelectedItem().toString().equals("Seleccione"))) {
                    if (fotocaptada.getDrawable() == null) {
                        valorFoto = false;
                    } else {
                        valorFoto = true;
                    }

                    publicar(valorFoto);
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Por favor, seleccione el área", Toast.LENGTH_LONG).show();
                }

            }
        });


        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public void publicar(boolean val) {

        RequestParams params1 = new RequestParams();
        try {

            if (val) {
                File imagen = new File(resultUriRecortada.getPath());
                params1.put("imagen", imagen);
            }

            params1.put("usuario_id", pref.getIdUsuarioPref());
            params1.put("distrito_id", pref.getDistritoIdPref());
            params1.put("destino", arrayArea.get(spn_areas.getSelectedItemPosition() - 1).getArea_nombre());
            params1.put("queja", et_post.getText().toString());


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(70000);
        client.post("http://" + IP + "/index.php?c=Pueblo&a=guardar&key_mobile=123456asdfgh", params1, new AsyncHttpResponseHandler() {

            String respuesta = null;
            ProgressDialog loading;

            @Override
            public void onStart() {
                super.onStart();
                loading = new ProgressDialog(context);
                loading.setTitle("SIPSI");
                loading.setMessage("Por favor espere...");
                loading.setIndeterminate(false);
                loading.setCancelable(false);
                //loading.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode == 200) {

                    try {
                        respuesta = new String(responseBody, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (respuesta.equals("1")) {
                        // FragmentEquiposHijo.ActualizarEquipo();
                        //mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        //onRefresh();
                        Toast.makeText(context, "Guardado correctamente", Toast.LENGTH_SHORT).show();
                        adaptadorListadoQuejas.notifyDataSetChanged();



                    } else {
                        Toast.makeText(context, "Vuelva a intentarlo", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Error al registrar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                //super.onFinish();
                //mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);

                loading.dismiss();
                // ActualizarQueja()

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_CAMERA) {

                CropImage.activity(output).start(getContext(), this);
                //  CropImage.activity(output).setMaxCropResultSize(1080,566 ).setMinCropResultSize(800,400).start(this);
            } else if (requestCode == SELET_GALERRY) {

                Uri uri = result.getData();


                File f1, f2;
                f1 = new File(getRealPathFromUri(context, uri));
                String fname = f1.getName();


                f2 = new File(Environment.getExternalStorageDirectory() + "/Sipsi/", "Queja");
                f2.mkdirs();
                try {
                    FileUtils.copyFileToDirectory(f1, f2);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
                    values.put(MediaStore.MediaColumns.DATA, f2.toString() + "/" + fname);
                    context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    //Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();
                }

                CropImage.activity(uri).start(context, this);

            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult resultado = CropImage.getActivityResult(result);
            if (resultCode == RESULT_OK) {

                resultUriRecortada = resultado.getUri();

                fotocaptada.setImageBitmap(BitmapFactory.decodeFile(resultUriRecortada.getPath()));

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = resultado.getError();
                //Toast.makeText(getApplicationContext(),"Error"+error, Toast.LENGTH_SHORT).show();
                Toast.makeText(activity, "Error: Intente de nuevo", Toast.LENGTH_SHORT).show();

            }
        }

    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void selectImage() {
        final CharSequence[] items = {"Camara", "Galería", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Seleccione :");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Camara")) {
                    userChoosenTask = "Camara";

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    File carpetas = new File(Environment.getExternalStorageDirectory() + "/Sipsi/", "Queja");
                    carpetas.mkdirs();

                    String aleatorio = pref.getIdUsuarioPref() + "_" + et_post.getText().toString();
                    String nombre = aleatorio + ".jpg";
                    File imagen = new File(carpetas, nombre);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                        String authorities = context.getPackageName() + ".provider";
                        Uri imageUri = FileProvider.getUriForFile(context, authorities, imagen);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    } else {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));

                    }
                    output = Uri.fromFile(imagen);
                    startActivityForResult(intent, REQUEST_CAMERA);

                } else if (items[item].equals("Galería")) {
                    userChoosenTask = "Galería";


                    Intent intentgaleria = new Intent(Intent.ACTION_PICK);
                    intentgaleria.setType("image/*");
                    if (intentgaleria.resolveActivity(getActivity().getPackageManager()) != null) {


                        startActivityForResult(intentgaleria, SELET_GALERRY);
                    }
                } else if (items[item].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
}
