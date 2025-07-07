package android.zimerer.thiago.check_in;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;
    private LatLng currentLocation;
    private BancoDados bd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent it = getIntent();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("MapaCheckIn");

        bd = new BancoDados(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        double lat = it.getDoubleExtra("lat", 0), lng = it.getDoubleExtra("lng", 0);

        currentLocation = new LatLng(lat, lng);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent it = null;
        Bundle bun = new Bundle();
        if (item.getItemId() == R.id.back){
            it = new Intent(getBaseContext(), MainActivity.class);
        } else if (item.getItemId() == R.id.management) {
            it = new Intent(getBaseContext(), CheckInActivity.class);
        } else if (item.getItemId() == R.id.mostVisited) {
            it = new Intent(getBaseContext(), ReportActivity.class);
        }

        if(it != null) {
            startActivity(it);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));

        addMarkers();
    }

    private void addMarkers() {
        Cursor cursor = bd.buscarCheckinsCategoria();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String local = cursor.getString(cursor.getColumnIndexOrThrow("Local"));
                int visitas = cursor.getInt(cursor.getColumnIndexOrThrow("qtdVisitas"));
                String latStr = cursor.getString(cursor.getColumnIndexOrThrow("latitude"));
                String lngStr = cursor.getString(cursor.getColumnIndexOrThrow("longitude"));
                String categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"));

                try {
                    double lat = Double.parseDouble(latStr);
                    double lng = Double.parseDouble(lngStr);
                    LatLng posicao = new LatLng(lat, lng);

                    map.addMarker(new MarkerOptions()
                            .position(posicao)
                            .title(local)
                            .snippet("Categoria: " + categoria + " - Visitas: " + visitas));

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Erro nas coordenadas de: " + local, Toast.LENGTH_SHORT).show();
                }

            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}