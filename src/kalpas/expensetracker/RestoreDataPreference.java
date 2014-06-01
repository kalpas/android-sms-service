package kalpas.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class RestoreDataPreference extends DialogPreference {
    
    public static final String RESTORE_DATA_ACTION = RestoreDataPreference.class.getSimpleName() + ".restore";

    public RestoreDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setDialogMessage(R.string.dialog_restore_data_message);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        setDialogIcon(null);
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult){
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setAction(RESTORE_DATA_ACTION);
            getContext().startActivity(intent);
        }
    }

}
