package net.xcreen.restsms;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class HomeFragment extends Fragment {

    Button toggleServerBtn;
    AppContext appContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        appContext = (AppContext) getActivity().getApplication();

        toggleServerBtn = rootView.findViewById(R.id.toggle_server_btn);
        toggleServerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if Device has a Sim-Card
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
                    int primarySim = telephonyManager.getSimState();
                    int secondarySim = TelephonyManager.SIM_STATE_ABSENT;
                    if (Build.VERSION.SDK_INT >= 26) {
                        primarySim = telephonyManager.getSimState(0);
                        secondarySim = telephonyManager.getSimState(1);
                    }
                    if (primarySim != TelephonyManager.SIM_STATE_READY) {
                        if (secondarySim != TelephonyManager.SIM_STATE_READY) {
                            //Device has not Sim-Card which is ready
                            Toast.makeText(getContext(), getResources().getText(R.string.invalid_sim), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                    Toast.makeText(getContext(), getResources().getText(R.string.invalid_sim), Toast.LENGTH_SHORT).show();
                    return;
                }

                //Check if Server is running
                if(appContext.smsServer.isRunning() && !appContext.smsServer.isStopping()){
                    //Stop Server
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                appContext.smsServer.stop();
                            }
                            catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }
                    }).start();
                    //Switch Button-Text
                    toggleServerBtn.setText(getResources().getText(R.string.start_server));
                }
                else if(!appContext.smsServer.isStopping()){
                    //Start Server
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                appContext.smsServer.start();
                            }
                            catch (Exception ex){
                                ex.printStackTrace();
                                Toast.makeText(getContext(), getResources().getText(R.string.server_failed_to_start), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).start();
                    //Switch Button-Text
                    toggleServerBtn.setText(getResources().getText(R.string.stop_server));
                }
                else{
                    //Server is stopping
                    Toast.makeText(getContext(), getResources().getText(R.string.server_is_stopping), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Check Server is running, to set correct Button-Text
        if(appContext.smsServer.isRunning()){
            toggleServerBtn.setText(getResources().getText(R.string.stop_server));
        }

        return rootView;
    }
}
