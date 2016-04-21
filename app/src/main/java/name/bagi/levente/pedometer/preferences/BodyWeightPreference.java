package name.bagi.levente.pedometer.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.nuist.yjn.R;

public class BodyWeightPreference extends EditMeasurementPreference {

    public BodyWeightPreference(Context context) {
        super(context);
    }

    public BodyWeightPreference(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public BodyWeightPreference(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    protected void initPreferenceDetails() {
        mTitleResource = R.string.body_weight_setting_title;  //体重
        mMetricUnitsResource = R.string.kilograms; //公斤
        mImperialUnitsResource = R.string.pounds;  //英镑
    }
}