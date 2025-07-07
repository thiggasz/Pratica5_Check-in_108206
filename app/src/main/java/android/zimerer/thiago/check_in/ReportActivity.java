package android.zimerer.thiago.check_in;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReportActivity extends AppCompatActivity {
    private BancoDados bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Relatório");

        bd = new BancoDados(this);

        populateCheckins();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent it = null;
        if (item.getItemId() == R.id.back) {
            it = new Intent(getBaseContext(), MainActivity.class);
        }

        if(it != null) {
            startActivity(it);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateCheckins() {
        LinearLayout locationsLayout = findViewById(R.id.locationsLayout);
        LinearLayout timesVisitedLayout = findViewById(R.id.timesVisitedLayout);

        clear(locationsLayout);
        clear(timesVisitedLayout);

        Cursor cursor = bd.buscarCheckinsCategoria();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String local = cursor.getString(cursor.getColumnIndexOrThrow("Local"));
                int visitas = cursor.getInt(cursor.getColumnIndexOrThrow("qtdVisitas"));

                TextView tvLocal = createTextView(local, Gravity.START);
                locationsLayout.addView(tvLocal);

                TextView tvVisitas = createTextView(String.valueOf(visitas), Gravity.END);
                timesVisitedLayout.addView(tvVisitas);

            } while (cursor.moveToNext());
            cursor.close();
        }
        bd.fechar();
    }

    private TextView createTextView(String text, int gravity) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setGravity(gravity);
        textView.setTextSize(16);
        textView.setPadding(0, 18, 0, 8);
        return textView;
    }

    private void clear(LinearLayout layout) {
        if (layout.getChildCount() > 1) {
            layout.removeViews(1, layout.getChildCount() - 1);
        }
    }
}