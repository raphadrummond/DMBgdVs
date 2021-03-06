package databit.com.br.datamobile.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import databit.com.br.datamobile.R;
import databit.com.br.datamobile.dao.LogsenhaDAO;
import databit.com.br.datamobile.dao.UsuarioDAO;
import databit.com.br.datamobile.domain.Logsenha;
import databit.com.br.datamobile.domain.Usuario;
import databit.com.br.datamobile.infra.Internet;
import databit.com.br.datamobile.wsclient.UsuarioWSClient;

public class SenhaActivity extends AppCompatActivity {

    private Usuario usuario;
    private Button btnGravar;
    private TextView edtAnterior;
    private TextView edtNova;
    private TextView edtConfirmacao;
    private CheckBox chExibir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senha);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        usuario = (Usuario) getIntent().getSerializableExtra("usuario");
        edtAnterior = (TextView)  findViewById(R.id.editSenhaant);
        edtNova = (TextView)  findViewById(R.id.editSenhanova);
        edtConfirmacao = (TextView)  findViewById(R.id.editSenhaconf);

        chExibir = (CheckBox) findViewById(R.id.checkExibir);
        chExibir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chExibir.isChecked() == true) {
                    edtAnterior.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    edtNova.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    edtConfirmacao.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else {
                    edtAnterior.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    edtNova.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    edtConfirmacao.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        btnGravar = (Button) findViewById(R.id.btnGravar);
        btnGravar.setText("Gravar");
        btnGravar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sAnterior = "";
                String sNova = "";
                String sConfirmacao =  "";
                try {
                    sAnterior = edtAnterior.getText().toString();
                } catch (Exception e) {
                }

                try {
                    sNova = edtNova.getText().toString();
                } catch (Exception e) {
                }

                try {
                    sConfirmacao = edtConfirmacao.getText().toString();
                } catch (Exception e) {
                }

                if (sAnterior.equals("")) {
                    Toast.makeText(SenhaActivity.this, "Senha anterior ?? preenchimento Obrigat??rio !", Toast.LENGTH_LONG).show();
                    edtAnterior.requestFocus();
                    return;
                }
                if (sNova.equals("")) {
                    Toast.makeText(SenhaActivity.this, "Nova senha ?? preenchimento Obrigat??rio !", Toast.LENGTH_LONG).show();
                    edtNova.requestFocus();
                    return;
                }
                if (sConfirmacao.equals("")) {
                    Toast.makeText(SenhaActivity.this, "Confirma????o de senha ?? preenchimento Obrigat??rio !", Toast.LENGTH_LONG).show();
                    edtNova.requestFocus();
                    return;
                }
                if (sNova.equals(usuario.getSenha().toString())) {
                    Toast.makeText(SenhaActivity.this, "A nova senha n??o pode ser igual com a senha anterior !", Toast.LENGTH_LONG).show();
                    edtNova.requestFocus();
                    return;
                }
                if (!(sAnterior.equals(usuario.getSenha().toString()))) {
                    Toast.makeText(SenhaActivity.this, "A senha anterior digitada n??o bate com a senha do usu??rio !", Toast.LENGTH_LONG).show();
                    edtAnterior.requestFocus();
                    return;
                }
                if (!(sNova.equals(sConfirmacao))) {
                    Toast.makeText(SenhaActivity.this, "A senha nova e a confirma????o n??o conferem !", Toast.LENGTH_LONG).show();
                    edtConfirmacao.requestFocus();
                    return;
                }
                autorizacao();
            }
        });

    }

    private void autorizacao() {
        SenhaActivity.AutorizacaoAsyncTask task = new SenhaActivity.AutorizacaoAsyncTask();
        task.execute();
    }

    class AutorizacaoAsyncTask extends AsyncTask<Void, Void,Void> {

        private ProgressDialog progressDialog;
        private boolean bErro = false;
        private String sResult;
        private String sErro;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(SenhaActivity.this, null, "Alterando Senha !");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            sResult="";
            try {
                if ((Internet.isDeviceOnline(getBaseContext())) && (Internet.urlOnline(getBaseContext()))) {
                    UsuarioWSClient usuarioWSClient = new UsuarioWSClient();
                    UsuarioDAO usuarioDAO = new UsuarioDAO();
                    Logsenha logsenha = new Logsenha();
                    LogsenhaDAO logsenhaDAO = new LogsenhaDAO();
                    usuario.setSenha(edtNova.getText().toString());
                    sResult = usuarioWSClient.enviaSenha(usuario);
                    if (sResult.equals("OK")) {
                        bErro = false;
                        usuarioDAO.gravaUsuario(usuario);
                        logsenha.setUsuario(usuario.getNome().toString());
                        logsenhaDAO.gravaLogsenha(logsenha);
                    }
                    else {
                        bErro = true;
                        sErro = sResult;
                    }
                }

            } catch (Throwable t) {
                bErro = true;
                sResult = t.getMessage().toString();
                t.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (bErro == false) {
                Toast.makeText(SenhaActivity.this, "Senha alterada com SUCESSO! Por quest??es de seguran??a o sistema ir?? SINCRONIZAR OS DADOS do seu dispositivo.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SenhaActivity.this, SincronizaActivity.class);
                intent.putExtra("novo",false);
                intent.putExtra("auto",true);
                intent.putExtra("usuario",usuario);
                startActivity(intent);
                finish();
            }
            else {
                Toast.makeText(SenhaActivity.this, "N??o foi poss??vel alterar a senha : " + sResult, Toast.LENGTH_SHORT).show();
            }
            try {
                progressDialog.dismiss();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

}
