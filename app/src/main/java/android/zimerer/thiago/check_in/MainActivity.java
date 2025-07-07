package android.zimerer.thiago.check_in;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements LocationListener {
    TextView latView, longView;
    Double lastLat, lastLong;
    private LocationManager lm;
    private Spinner spinner;
    private BancoDados bd;
    private AutoCompleteTextView autoComplete;
    private SimpleCursorAdapter autoCompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("CheckInLocais");

        latView = (TextView) findViewById(R.id.latitudeValue);
        longView = (TextView) findViewById(R.id.longitudeValue);
        autoComplete = (AutoCompleteTextView) findViewById(R.id.searchField);
        spinner = findViewById(R.id.categorySpinner);

        bd = new BancoDados(this);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        }

        getCategories();

        autoCompleteOptions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent it = null;
        Bundle bun = new Bundle();
        if (item.getItemId() == R.id.maps){
            if(lastLat == null || lastLong == null) {
                Toast.makeText(this, "Localização não definida", Toast.LENGTH_SHORT).show();
                return false;
            }
            it = new Intent(getBaseContext(), MapActivity.class);
            bun.putDouble("lat", lastLat);
            bun.putDouble("lng", lastLong);
            it.putExtras(bun);
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

    @Override
    public void onLocationChanged(Location location) {
        lastLat = location.getLatitude();
        lastLong = location.getLongitude();
        latView.setText(String.valueOf(lastLat));
        longView.setText(String.valueOf(lastLong));
    }

    private void getCategories() {
        Cursor cursor = bd.buscarCategorias();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_item,
                cursor,
                new String[]{"nome"},
                new int[]{android.R.id.text1},
                0
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void onClickCheckIn(View v) {
        String local = autoComplete.getText().toString().trim();
        long categoriaId = spinner.getSelectedItemId();
        String latitude = latView.getText().toString();
        String longitude = longView.getText().toString();

        if (local.isEmpty() || categoriaId == Spinner.INVALID_ROW_ID || latitude.isEmpty() || longitude.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos e aguarde a localização!", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor c = bd.buscar("Checkin", new String[]{"Local", "qtdVisitas"}, "Local = '" + local + "'", "");

        if (c.getCount() > 0) {
            c.moveToFirst();
            int visits = c.getInt(c.getColumnIndexOrThrow("qtdVisitas"));

            ContentValues cv = new ContentValues();
            cv.put("qtdVisitas", visits + 1);

            bd.atualizar("Checkin", cv, "Local = '" + local.replace("'", "''") + "'");
            Toast.makeText(this, "Check-in atualizado! Visitas: " + (visits + 1), Toast.LENGTH_SHORT).show();
        } else {
            ContentValues cv = new ContentValues();
            cv.put("Local", local);
            cv.put("qtdVisitas", 1);
            cv.put("cat", categoriaId);
            cv.put("latitude", latitude);
            cv.put("longitude", longitude);

            bd.inserir("Checkin", cv);
            Toast.makeText(this, "Novo check-in registrado!", Toast.LENGTH_SHORT).show();
        }
        c.close();

        finish();
        startActivity(getIntent());
    }

    private void autoCompleteOptions() {
        autoCompleteAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                null,
                new String[]{"_id"},
                new int[]{android.R.id.text1},
                0
        );

        autoCompleteAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return bd.buscarLocais(constraint != null ? constraint.toString() : "");
            }
        });

        autoCompleteAdapter.setCursorToStringConverter(cursor ->
                cursor.getString(cursor.getColumnIndexOrThrow("_id"))
        );

        autoComplete.setAdapter(autoCompleteAdapter);
        autoComplete.setThreshold(1);
    }
}