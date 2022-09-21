package edu.byu.cs.tweeter.client.service;

import android.os.Bundle;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.widget.Toast;

import edu.byu.cs.client.R;
import edu.byu.cs.tweeter.client.backgroundTask.FollowTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetFollowersCountTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetFollowersTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetFollowingCountTask;
import edu.byu.cs.tweeter.client.backgroundTask.GetFollowingTask;
import edu.byu.cs.tweeter.client.backgroundTask.IsFollowerTask;
import edu.byu.cs.tweeter.client.backgroundTask.UnfollowTask;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowService {

    public interface GetFollowObserver {
        void handleSuccess(List<User> followees, boolean hasMorePages);
        void handleFailure(String message);
        void handleException(Exception ex);
    }

    public interface IsFollowerObserver {
        void handleIsFollowerSuccess(boolean isFollower);
        void handleIsFollowerFailure(String message);
        void handleIsFollowerException(Exception ex);
    }

    public interface UnfollowObserver {
        void handleUnfollowSuccess();
        void handleUnfollowFailure(String message);
        void handleUnfollowException(Exception ex);
    }

    public interface FollowObserver {
        void handleFollowSuccess();
        void handleFollowFailure(String message);
        void handleFollowException(Exception ex);
    }

    public interface GetFollowersCountObserver{
        void handleFollowersCountSuccess(int count);
        void handleFollowersCountFailure(String message);
        void handleFollowersCountException(Exception ex);
    }

    public interface GetFollowingCountObserver{
        void handleFollowingCountSuccess(int count);
        void handleFollowingCountFailure(String message);
        void handleFollowingCountException(Exception ex);
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

    public void isFollower(AuthToken authToken, User currUser, User targetUser, IsFollowerObserver observer){
        IsFollowerTask isFollowerTask = new IsFollowerTask(authToken, currUser, targetUser, new IsFollowerHandler(observer));
    }

    public void unfollow(AuthToken authToken, User targetUser, UnfollowObserver observer){
        UnfollowTask unfollowTask = new UnfollowTask(authToken, targetUser, new UnfollowHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(unfollowTask);
    }

    public void follow(AuthToken authToken, User targetUser, FollowObserver observer){
        FollowTask followTask = new FollowTask(authToken, targetUser, new FollowHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(followTask);
    }

    public void updateSelectedUserFollowingAndFollowers(AuthToken authToken, User targetUser,
                                                        GetFollowersCountObserver observerFR, GetFollowingCountObserver observerFG){
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Get count of most recently selected user's followers.
        GetFollowersCountTask followersCountTask = new GetFollowersCountTask(authToken, targetUser, new GetFollowersCountHandler(observerFR));
        executor.execute(followersCountTask);

        // Get count of most recently selected user's followees (who they are following)
        GetFollowingCountTask followingCountTask = new GetFollowingCountTask(authToken, targetUser, new GetFollowingCountHandler(observerFG));
        executor.execute(followingCountTask);
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

    private class IsFollowerHandler extends Handler {
        private IsFollowerObserver observer;

        public IsFollowerHandler(IsFollowerObserver observer){
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(IsFollowerTask.SUCCESS_KEY);
            if (success) {
                boolean isFollower = msg.getData().getBoolean(IsFollowerTask.IS_FOLLOWER_KEY);

                observer.handleIsFollowerSuccess(isFollower);

                // If logged in user if a follower of the selected user, display the follow button as "following"

            } else if (msg.getData().containsKey(IsFollowerTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(IsFollowerTask.MESSAGE_KEY);
                observer.handleIsFollowerFailure(message);
            } else if (msg.getData().containsKey(IsFollowerTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(IsFollowerTask.EXCEPTION_KEY);
                observer.handleIsFollowerException(ex);
            }
        }
    }

    private class FollowHandler extends Handler {
        private FollowObserver observer;

        public FollowHandler(FollowObserver observer){
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(FollowTask.SUCCESS_KEY);
            if (success) {
                observer.handleFollowSuccess();
            } else if (msg.getData().containsKey(FollowTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(FollowTask.MESSAGE_KEY);
                observer.handleFollowFailure(message);
            } else if (msg.getData().containsKey(FollowTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(FollowTask.EXCEPTION_KEY);
                observer.handleFollowException(ex);
            }
        }
    }

    private class UnfollowHandler extends Handler {
        private UnfollowObserver observer;

        public UnfollowHandler(UnfollowObserver observer){
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(UnfollowTask.SUCCESS_KEY);
            if (success) {
                observer.handleUnfollowSuccess();
            } else if (msg.getData().containsKey(UnfollowTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(UnfollowTask.MESSAGE_KEY);
                observer.handleUnfollowFailure(message);
            } else if (msg.getData().containsKey(UnfollowTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(UnfollowTask.EXCEPTION_KEY);
                observer.handleUnfollowException(ex);
            }
        }
    }

    private class GetFollowersCountHandler extends Handler {
        private GetFollowersCountObserver observer;

        public GetFollowersCountHandler(GetFollowersCountObserver observer){
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowersCountTask.SUCCESS_KEY);
            if (success) {
                int count = msg.getData().getInt(GetFollowersCountTask.COUNT_KEY);
                observer.handleFollowersCountSuccess(count);
            } else if (msg.getData().containsKey(GetFollowersCountTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowersCountTask.MESSAGE_KEY);
                observer.handleFollowersCountFailure(message);
            } else if (msg.getData().containsKey(GetFollowersCountTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowersCountTask.EXCEPTION_KEY);
                observer.handleFollowersCountException(ex);
            }
        }
    }

    // GetFollowingCountHandler

    private class GetFollowingCountHandler extends Handler {
        private GetFollowingCountObserver observer;

        public GetFollowingCountHandler(GetFollowingCountObserver observer){
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(GetFollowingCountTask.SUCCESS_KEY);
            if (success) {
                int count = msg.getData().getInt(GetFollowingCountTask.COUNT_KEY);
                observer.handleFollowingCountSuccess(count);

            } else if (msg.getData().containsKey(GetFollowingCountTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(GetFollowingCountTask.MESSAGE_KEY);
                observer.handleFollowingCountFailure(message);
            } else if (msg.getData().containsKey(GetFollowingCountTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(GetFollowingCountTask.EXCEPTION_KEY);
                observer.handleFollowingCountException(ex);
            }
        }
    }
}
