package io.givenow.app.fragments;

/**
 * Created by aphex on 11/26/15.
 */

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.givenow.app.R;
import io.givenow.app.helpers.CustomAnimations;
import io.givenow.app.interfaces.AnimatorEndListener;
import io.givenow.app.models.ParseUserHelper;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;

/**
 * Created by aphex on 11/23/15.
 */
public class PhoneNumberVerificationFragment extends Fragment {

    @Bind(R.id.description)
    TextView tvDescription;
    @Bind(R.id.etPhoneNumber)
    EditText etPhoneNumber;
    @Bind(R.id.etSMSCode)
    EditText etSMSCode;
    @Bind(R.id.back)
    ImageButton ibBack;
    @Bind(R.id.done)
    ImageButton ibDone;
    @Bind(R.id.vsPhoneSMS)
    ViewSwitcher vsPhoneSMS;
    private boolean mPhoneNumberFieldShowing = true;

    private OnUserLoginCompleteListener mListener;

    public PhoneNumberVerificationFragment() {
    }

    public static PhoneNumberVerificationFragment newInstance() {
        PhoneNumberVerificationFragment phoneNumberVerificationFragment = new PhoneNumberVerificationFragment();

        return phoneNumberVerificationFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_phone_verification, container, false);
        ButterKnife.bind(this, v);

//        llMain.setBackgroundColor(colour);

        etPhoneNumber.addTextChangedListener(new android.telephony.PhoneNumberFormattingTextWatcher()); //new PhoneNumberFormattingTextWatcher(Locale.getDefault().getCountry()));
        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) { //TODO or if valid phone number
                    if (ibDone.getVisibility() != View.VISIBLE) {
                        CustomAnimations.circularReveal(ibDone).start();
                    }
                } else {
                    if (ibDone.getVisibility() == View.VISIBLE) {
                        CustomAnimations.circularHide(ibDone).start();
                    }
                }
            }
        });

        etSMSCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4) {
                    if (ibDone.getVisibility() != View.VISIBLE) {
                        CustomAnimations.circularReveal(ibDone).start();
                    }
                } else {
                    if (ibDone.getVisibility() == View.VISIBLE) {
                        CustomAnimations.circularHide(ibDone).start();
                    }
                }
            }
        });
        vsPhoneSMS.setInAnimation(getActivity(), android.R.anim.slide_in_left);
        vsPhoneSMS.setOutAnimation(getActivity(), android.R.anim.slide_out_right);

        tvDescription.setText(R.string.phone_number_disclaimer);
        return v;
    }

    public String getPhoneNumber() {
        return etPhoneNumber.getText().toString();
    }

    @OnClick(R.id.done)
    public void onDonePressed(ImageButton ibDone) {
        ibDone.setClickable(false);
        if (mPhoneNumberFieldShowing)
            sendCode();
        else
            doLogin();
    }

    @OnClick(R.id.back)
    public void onIbBackPressed(ImageButton ibBack) {
        phoneNumberUI();
        if (getPhoneNumber().length() > 0) {
            CustomAnimations.circularReveal(ibDone).start();
        }
    }

    private void phoneNumberUI() {
        vsPhoneSMS.setDisplayedChild(0);
        mPhoneNumberFieldShowing = true;
        CustomAnimations.circularHide(ibBack).start();
        tvDescription.setText(R.string.phone_number_disclaimer);
        ibDone.setClickable(true);
    }

    private void codeUI(String phoneNumber) {
        vsPhoneSMS.setDisplayedChild(1);
        mPhoneNumberFieldShowing = false;
        tvDescription.setText(getString(R.string.validate_sms_code, phoneNumber));
        CustomAnimations.circularReveal(ibBack).start();
        ibDone.setClickable(true);
    }

    private void sendCode() {
        String phoneNumber = getPhoneNumber();

        if (phoneNumber.length() > 0) {
            //validate phone number
//            PhoneNumberUtil.getInstance().parse(phoneNumber)
            //change done button to spinner
            CustomAnimations.circularHide(ibDone).start();

            //if phone number is valid
            //call sendCode
            ParseUserHelper.sendCode(phoneNumber, getString(R.string.sms_body_javascript)).subscribe(
                    response -> {
                        Log.d("Cloud Response", response.toString());
                        //switch to sendSMS edittext
                        codeUI(phoneNumber);
                    },
                    error -> {
                        Log.d("Cloud Response", "Error received from sendCode cloud function: ", error);
                        CustomAnimations.circularReveal(ibDone).start();
                        phoneNumberUI();
                    });
        } else { //Or just hide ibdone
            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
            ibDone.startAnimation(shake);
            ibDone.setClickable(true);
        }

    }

    private void doLogin() {
        if (etSMSCode.getText().toString().length() == 4) {
            CustomAnimations.circularHide(ibDone).start();

            String phoneNumber = getPhoneNumber();
            int code = Integer.parseInt(etSMSCode.getText().toString());
            ParseUserHelper.logIn(phoneNumber, code).subscribe(
                    sessionToken -> {
                        // ParseUserHelper.signUpOrLogin(phoneNumber, this::onUserLoginCompleteAction);
                        ParseObservable.become(sessionToken.toString()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(becameUser -> {
                                    Log.d("Onboarding", "Became user " + becameUser.getUsername());
                                    ParseUserHelper.associateWithDevice(becameUser);
                                    userLoginComplete();
                                });
                    },
                    error -> {
                        Log.d("Cloud Response", "Error received from logIn cloud function: ", error);
                        phoneNumberUI();
                    }
            );
        }
    }

    private void userLoginComplete() {
        //change done button to givenow smiley
        ibDone.setImageResource(R.mipmap.ic_launcher);
        Animator reveal = CustomAnimations.circularReveal(ibDone);
        reveal.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mListener.onUserLoginComplete();
            }
        });
        reveal.start();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (OnUserLoginCompleteListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnUserLoginCompleteListener");
        }
    }

    // Container Activity must implement this interface
    public interface OnUserLoginCompleteListener {
        void onUserLoginComplete();
    }
}