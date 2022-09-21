package edu.byu.cs.tweeter.client.service;

import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;

import edu.byu.cs.tweeter.client.backgroundTask.PostStatusTask;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class StatusService {
    private List<NewStatusObserver> observers;

    public StatusService(List<NewStatusObserver> observers){
        this.observers = observers;
    }

    public StatusService(){
        this.observers = new ArrayList<NewStatusObserver>();
    }

    /**
     * Attaches a NewstatusObserver object
     * @param observer
     */
    public void AttachNewStatusObserver(NewStatusObserver observer){
        this.observers.add(observer);
    }

    /**
     * Detaches the observer object from this service
     * @param observer
     */
    public void DetachNewStatusObserver(NewStatusObserver observer){
        this.observers.remove(observer);
    }

    /**
     * Sends the post to all observers listening to this post
     * @param post
     */
    public void NewStatusNotify(String post){
        for(NewStatusObserver o : observers){
            o.onStatusPosted(post);
        }
    }

    public interface NewStatusObserver {
        void onStatusPosted(String post);
    }

    public interface CreatedStatusObserver{
        void handleStatusSuccess();
        void handleStatusFailure(String message);
        void handleStatusException(Exception ex);
    }

    public void createStatus(String post, User user, String formattedDateTime, List<String> parsedUrls,
                          List<String> parsedMentions, CreatedStatusObserver observer){
        Status newStatus = new Status(post, user, formattedDateTime, parsedUrls, parsedMentions);
        PostStatusTask statusTask = new PostStatusTask(Cache.getInstance().getCurrUserAuthToken(),
                newStatus, new PostStatusHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(statusTask);
    }

    private class PostStatusHandler extends Handler {
        private CreatedStatusObserver observer;

        public PostStatusHandler(CreatedStatusObserver observer){
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(PostStatusTask.SUCCESS_KEY);
            if (success) {
                observer.handleStatusSuccess();
            } else if (msg.getData().containsKey(PostStatusTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(PostStatusTask.MESSAGE_KEY);
                observer.handleStatusFailure(message);
            } else if (msg.getData().containsKey(PostStatusTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(PostStatusTask.EXCEPTION_KEY);
                observer.handleStatusException(ex);
            }
        }
    }


}
