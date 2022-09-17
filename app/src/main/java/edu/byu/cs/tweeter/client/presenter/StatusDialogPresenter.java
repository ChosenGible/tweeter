package edu.byu.cs.tweeter.client.presenter;

import android.net.Uri;

import java.util.Locale;

import edu.byu.cs.tweeter.client.service.NewStatusService;

public class StatusDialogPresenter {
    private StatusDialogView view;
    private NewStatusService newStatusService;

    public StatusDialogPresenter(StatusDialogView view){
        this.view = view;
        newStatusService = new NewStatusService();
    }


    //methods the view can call on this presenter
    public void sendNewStatus(String postText){
        newStatusService.NewStatusNotify(postText);
    }

    public void addStatusObserver(Object observer){
        try {
            newStatusService.AttachNewStatusObserver((NewStatusService.NewStatusObserver) observer);
        } catch (ClassCastException e) {
            throw new ClassCastException(observer.toString() + " must implement the NewStatusService.NewStatusObserver");
        }

    }

    public void updateWordCount(String changedText){
        String trim = changedText.trim();
        if (trim.isEmpty()){
            view.updateWordCount(0);
        }
        else{
            view.updateWordCount(trim.split("\\s+").length);
        }
    }

    public interface StatusDialogView{
        void displayFullName(String name);
        void displayAlias(String alias);
        void displayImage(Uri image);
        void displayPost(String postText);
        void fetchPostText();
        void updateWordCount(int numWords);
    }
}
