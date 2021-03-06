package app.beedle.pocketreview.Activity;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import app.beedle.pocketreview.listener.CurrencyItemClickListener;
import app.beedle.pocketreview.R;
import app.beedle.pocketreview.adapter.CurrencyAdapter;
import app.beedle.pocketreview.api.FixerInterface;
import app.beedle.pocketreview.model.Currency;
import app.beedle.pocketreview.model.CurrencyExchange;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CurrencyActivity extends AppCompatActivity implements Callback<CurrencyExchange>, CurrencyItemClickListener {

    private ListView lvCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_tab);
        setBinding();
        setToolbar();


    }

    private void setBinding() {
        lvCurrency = findViewById(R.id.lvCurrency);
    }

    private void setToolbar() {
        Toolbar tbMain = findViewById(R.id.tbAddNote);
        setSupportActionBar(tbMain);
        getSupportActionBar().setTitle("Pocket Review");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tbMain.setNavigationIcon(getResources().getDrawable(R.drawable.ic_navigate_before_black_24px));
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadCurrencyExchangeData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadCurrencyExchangeData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.fixer.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FixerInterface service = retrofit.create(FixerInterface.class); //Currency Interface Service
        Call<CurrencyExchange> call = service.loadCurrencyExchange();
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<CurrencyExchange> call, Response<CurrencyExchange> response) {
        CurrencyExchange currencyExchange = response.body();

        lvCurrency.setAdapter(new CurrencyAdapter(this, currencyExchange.getCurrencyList(), this));
    }

    @Override
    public void onFailure(Call<CurrencyExchange> call, Throwable t) {
        Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCurrencyItemClick(Currency c) {

    }
}
