package kalpas.expensetracker;

import kalpas.expensetracker.R;
import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class ClearAllPreference extends DialogPreference {
    
    public static final String CLEAR_ALL_ACTION = ClearAllPreference.class.getSimpleName() + ".clearAll";

    public ClearAllPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setDialogMessage(R.string.dialog_clear_all_message);
        setPositiveButtonText(R.string.dialog_clear_all);
        setNegativeButtonText(android.R.string.cancel);
        
        setDialogIcon(null);
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult){
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setAction(CLEAR_ALL_ACTION);
            getContext().startActivity(intent);
        }
    }

}
