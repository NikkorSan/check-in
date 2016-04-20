import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jernil on 4/10/2016.
 */
public class timercode {
    private Timer timer;
    private TimerTask timerTask;

    public void onPause(){
        super.onPause();
        timer.cancel();
    }

    public void onResume(){
        super.onResume();
        try {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    //Download file here and refresh
                }
            };
            timer.schedule(timerTask, 30000, 30000);
        } catch (IllegalStateException e){
            android.util.Log.i("Damn", "resume error");
        }
    }
}
