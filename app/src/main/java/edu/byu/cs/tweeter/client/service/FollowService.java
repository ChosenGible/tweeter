package edu.byu.cs.tweeter.client.service;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

import edu.byu.cs.tweeter.client.backgroundTask.GetFollowingTask;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowService {

    public interface GetFollowingObserver {
        void handleSuccess(List<User> followees, boolean hasMorePages);
        void handleFailure(String message);
        void handleException(Exception ex);
    }

    public FollowService() {}


    public void getFollowees(AuthToken authToken, User targetUser, int limit, User lastfollowee, GetFollowingObserver observer){
        GetFollowingTask followingTask = getGetFollowingTask(authToken, targetUser, limit, lastfollowee, observer);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(followingTask);
    }

    public GetFollowingTask getGetFollowingTask(AuthToken authToken, User targetUser, int limit, User lastFollowee, GetFollowingObserver observer){
        return new GetFollowingTask(authToken, targetUser, limit, lastFollowee, new MessageHandler(observer));
    }


    public class MessageHandler extends Handler{
        private final GetFollowingObserver observer;

        public MessageHandler(GetFollowingObserver observer) {
            //super(Looper.getMainLooper());
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message message) {
            Bundle bundle = message.getData();
            boolean success = bundle.getBoolean(GetFollowingTask.SUCCESS_KEY);
            if (success) {
                List<User> followees = (List<User>)bundle.getSerializable(GetFollowingTask.FOLLOWEES_KEY);
                boolean hasMorePages = bundle.getBoolean(GetFollowingTask.MORE_PAGES_KEY);
                observer.handleSuccess(followees, hasMorePages);
            }
            else if (bundle.containsKey(GetFollowingTask.MESSAGE_KEY)){
                String errorMessage = bundle.getString(GetFollowingTask.MESSAGE_KEY);
                observer.handleFailure(errorMessage);
            }
            else if (bundle.containsKey(GetFollowingTask.EXCEPTION_KEY)){
                Exception ex = (Exception) bundle.getSerializable(GetFollowingTask.EXCEPTION_KEY);
                observer.handleException(ex);
            }
        }
    }
}
