package edu.byu.cs.tweeter.client.service;

import android.os.Bundle;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

import edu.byu.cs.tweeter.client.backgroundTask.GetFollowersTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetFollowingTask;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowService {

    public interface GetFollowObserver {
        void handleSuccess(List<User> followees, boolean hasMorePages);
        void handleFailure(String message);
        void handleException(Exception ex);
    }

    public FollowService() {}


    public void getFollowees(AuthToken authToken, User targetUser, int limit, User lastFollowee, GetFollowObserver observer){
        GetFollowingTask followingTask = getGetFollowingTask(authToken, targetUser, limit, lastFollowee, observer);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(followingTask);
    }

    public void getFollowers(AuthToken authToken, User targetUser, int limit, User lastFollower, GetFollowObserver observer){
        GetFollowersTask followersTask = getGetFollowersTask(authToken, targetUser, limit, lastFollower, observer);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(followersTask);
    }

    public GetFollowingTask getGetFollowingTask(AuthToken authToken, User targetUser, int limit, User lastFollowee, GetFollowObserver observer){
        return new GetFollowingTask(authToken, targetUser, limit, lastFollowee, new FollowingMessageHandler(observer));
    }

    public GetFollowersTask getGetFollowersTask(AuthToken authToken, User targetUser, int limit, User lastFollower, GetFollowObserver observer){
        return new GetFollowersTask(authToken, targetUser, limit, lastFollower, new FollowersMessageHandler(observer));
    }


    private class FollowingMessageHandler extends Handler{
        private final GetFollowObserver observer;

        public FollowingMessageHandler(GetFollowObserver observer) {
            //super(Looper.getMainLooper());
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message message) {
            Bundle bundle = message.getData();
            boolean success = bundle.getBoolean(GetFollowingTask.SUCCESS_KEY);
            if (success) {
                List<User> followees = (List<User>)bundle.getSerializable(GetFollowingTask.FOLLOWEES_KEY);
                if (followees == null){
                    observer.handleFailure("no followees");
                }
                else {
                    boolean hasMorePages = bundle.getBoolean(GetFollowingTask.MORE_PAGES_KEY);
                    observer.handleSuccess(followees, hasMorePages);
                }
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

    private class FollowersMessageHandler extends Handler{
        private final GetFollowObserver observer;

        public FollowersMessageHandler(GetFollowObserver observer) {
            //super(Looper.getMainLooper());
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message message) {
            Bundle bundle = message.getData();
            boolean success = bundle.getBoolean(GetFollowingTask.SUCCESS_KEY);
            if (success) {
                List<User> followers = (List<User>)bundle.getSerializable(GetFollowersTask.FOLLOWERS_KEY);
                if (followers == null){
                    observer.handleFailure("no followers");
                }
                else {
                    boolean hasMorePages = bundle.getBoolean(GetFollowingTask.MORE_PAGES_KEY);
                    observer.handleSuccess(followers, hasMorePages);
                }
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
