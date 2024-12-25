package in.co.gorest.grblcontroller;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import java.util.Objects;
import in.co.gorest.grblcontroller.adapters.NotificationAdapter;
import in.co.gorest.grblcontroller.events.FcmNotificationRecieved;
import in.co.gorest.grblcontroller.listeners.EndlessRecyclerViewScrollListener;
import in.co.gorest.grblcontroller.model.GrblNotification;

public class NotificationArchiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_archive);

        Objects.requireNonNull(getSupportActionBar()).setSubtitle("notification archive");

        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume(){
        super.onResume();

        final List<GrblNotification> dataSet = GrblNotification.find(GrblNotification.class, null, null, null, "id DESC", "0, 10");
        if(dataSet.size() == 0){
            TextView emptyView = findViewById(R.id.empty_view);
            emptyView.setVisibility(View.VISIBLE);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        final NotificationAdapter notificationAdapter = new NotificationAdapter(this, dataSet);
        recyclerView.setAdapter(notificationAdapter);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                String limit = page * 10 + ", 10";
                List<GrblNotification> moreItems = GrblNotification.find(GrblNotification.class, null, null, null, "id DESC", limit);
                dataSet.addAll(moreItems);
                notificationAdapter.notifyItemRangeInserted(notificationAdapter.getItemCount(), dataSet.size() - 1);
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFcmNotificationReceived(FcmNotificationRecieved notificationReceived){

    }

}
