package android.zimerer.thiago.check_in;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BancoDados {
    protected SQLiteDatabase db;

    private final String NOME_BANCO = "checkin_bd";

    private final String[] SCRIPT_DATABASE_CREATE = new String[] {
            "CREATE TABLE Checkin (Local TEXT PRIMARY KEY, qtdVisitas INTEGER NOT NULL," +
                    "cat INTEGER NOT NULL, latitude TEXT NOT NULL, longitude TEXT NOT NULL, " +
                    "CONSTRAINT fkey0 FOREIGN KEY (cat) REFERENCES Categoria (idCategoria));",
            "CREATE TABLE Categoria (idCategoria INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL);",
            "INSERT INTO Categoria (nome) VALUES ('Restaurante');",
            "INSERT INTO Categoria (nome) VALUES ('Bar');",
            "INSERT INTO Categoria (nome) VALUES ('Cinema');",
            "INSERT INTO Categoria (nome) VALUES ('Universidade');",
            "INSERT INTO Categoria (nome) VALUES ('Estádio');",
            "INSERT INTO Categoria (nome) VALUES ('Parque');",
            "INSERT INTO Categoria (nome) VALUES ('Outros');"};

    public BancoDados(Context ctx) {
        db = ctx.openOrCreateDatabase(NOME_BANCO, Context.MODE_PRIVATE, null);
        Cursor c = buscar("sqlite_master", null, "type = 'table'", "");
        if(c.getCount() == 1){
            for(int i = 0; i < SCRIPT_DATABASE_CREATE.length; i++){
                db.execSQL(SCRIPT_DATABASE_CREATE[i]);
            }
            Log.i("BANCO_DADOS", "Criou tabelas do banco e as populou.");
        }
        c.close();
        Log.i("BANCO_DADOS", "Abriu conexão com o banco.");
    }

    public long inserir(String tabela, ContentValues valores) {
        long id = db.insert(tabela, null, valores);
        Log.i("BANCO_DADOS", "Cadastrou registro com o id [" + id + "]");
        return id;
    }

    public int atualizar(String tabela, ContentValues valores, String where) {
        int count = db.update(tabela, valores, where, null);
        Log.i("BANCO_DADOS", "Atualizou [" + count + "] registros");
        return count;
    }

    public int deletar(String tabela, String where) {
        int count = db.delete(tabela, where, null);
        Log.i("BANCO_DADOS", "Deletou [" + count + "] registros");
        return count;
    }

    public Cursor buscar(String tabela, String colunas[], String where, String orderBy) {
        Cursor c;
        if(!where.isEmpty())
            c = db.query(tabela, colunas, where, null, null, null, orderBy);
        else
            c = db.query(tabela, colunas, null, null, null, null, orderBy);
        Log.i("BANCO_DADOS", "Realizou uma busca e retornou [" + c.getCount() + "] registros.");
        return c;
    }

    public void abrir(Context ctx) {

        db = ctx.openOrCreateDatabase(NOME_BANCO, Context.MODE_PRIVATE, null);
        Log.i("BANCO_DADOS", "Abriu conexão com o banco.");
    }

    public void fechar() {
        if (db != null) {
            db.close();
            Log.i("BANCO_DADOS", "Fechou conexão com o Banco.");
        }
    }

    public Cursor buscarCategorias() {
        return db.query("Categoria",
                new String[]{"idCategoria as _id", "nome"},
                null, null, null, null, "nome");
    }

    public Cursor buscarLocais(String filtro) {
        return db.query("Checkin",
                new String[]{"Local AS _id"},
                "Local LIKE ?",
                new String[]{"%" + filtro + "%"},
                null, null, "Local");
    }

    public Cursor buscarCheckinsCategoria() {
        String query = "SELECT Checkin.Local, Checkin.qtdVisitas, Checkin.latitude, " +
                "Checkin.longitude, Categoria.nome as categoria " +
                "FROM Checkin " +
                "INNER JOIN Categoria ON Checkin.cat = Categoria.idCategoria";
        return db.rawQuery(query, null);
    }
}
