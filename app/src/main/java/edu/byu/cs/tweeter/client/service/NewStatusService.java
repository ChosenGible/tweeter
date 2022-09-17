package edu.byu.cs.tweeter.client.service;

import java.util.ArrayList;
import java.util.List;

public class NewStatusService {
    private List<NewStatusObserver> observers;

    public NewStatusService(List<NewStatusObserver> observers){
        this.observers = observers;
    }

    public NewStatusService(){
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
}
