package hu.bme.aut.android.spotifydemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hu.bme.aut.android.spotifydemo.adapter.ArtistsAdapter;
import hu.bme.aut.android.spotifydemo.model.ArtistsResult;
import hu.bme.aut.android.spotifydemo.model.Item;
import hu.bme.aut.android.spotifydemo.model.Token;
import hu.bme.aut.android.spotifydemo.network.ArtistsApi;
import hu.bme.aut.android.spotifydemo.network.TokenApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistsFragment extends Fragment {

    private EditText etArtist;
    private RecyclerView recyclerViewArtists;
    private SwipeRefreshLayout swipeRefreshLayoutArtists;
    private TextView tvEmpty;
    private List<Item> artistsList;
    private ArtistsAdapter artistsAdapter;

    private String artist = "queen";
    private ArtistsApi artistsApi;
    private TokenApi tokenApi;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        artist = getActivity().getIntent().getStringExtra(MainActivity.KEY_ARTIST);

        Retrofit retrofitArtists = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        artistsApi = retrofitArtists.create(ArtistsApi.class);


        Retrofit retrofitToken = new Retrofit.Builder()
                .baseUrl("https://accounts.spotify.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        tokenApi = retrofitToken.create(TokenApi.class);


        refreshArtists();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists, container, false);
        etArtist = (EditText) view.findViewById(R.id.etArtist);
        etArtist.setText(artist);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);
        recyclerViewArtists = (RecyclerView) view.findViewById(R.id.recyclerViewArtists);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewArtists.setLayoutManager(llm);

        artistsList = new ArrayList<>();
        artistsAdapter = new ArtistsAdapter(getContext(), artistsList);
        recyclerViewArtists.setAdapter(artistsAdapter);

        swipeRefreshLayoutArtists = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayoutArtists);

        swipeRefreshLayoutArtists.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                artist = etArtist.getText().toString();
                refreshArtists();
            }
        });
        return view;
    }

    private void refreshArtists() {

        Call<Token> tokenQuery = tokenApi.getToken("client_credentials", "Basic MDM0MjY2ODU0OTI1NGZkOWFiMzdmMzNlZjRkNjRkYjA6ODM1MWFiMjVjYzVmNDBhMjg5OGI5N2U5ZjQyMmNkMDk=");
        tokenQuery.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                Call<ArtistsResult> artistsQuery = artistsApi.getArtists("Bearer " + response.body().getAccessToken(), artist,
                        "artist", 0, 3);
                artistsQuery.enqueue(new Callback<ArtistsResult>() {
                    @Override
                    public void onResponse(Call<ArtistsResult> call,
                                           Response<ArtistsResult> response) {
                        showArtists(response.body().getArtists().getItems());
                        if (swipeRefreshLayoutArtists != null) {
                            swipeRefreshLayoutArtists.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ArtistsResult> call, Throwable t) {
                        Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                        if (swipeRefreshLayoutArtists != null) {
                            swipeRefreshLayoutArtists.setRefreshing(false);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                if (swipeRefreshLayoutArtists != null) {
                    swipeRefreshLayoutArtists.setRefreshing(false);
                }
            }
        });


    }

    public void showArtists(List<Item> artists) {
        artistsList.clear();
        artistsList.addAll(artists);
        artistsAdapter.notifyDataSetChanged();

        if (artistsList.isEmpty()) {
            recyclerViewArtists.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewArtists.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
