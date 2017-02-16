package com.teramatrix.fleetiq.LoginMVP;

/**
 * Created by arun.singh on 9/28/2016.
 */
public class LoginPresenter {

    LoginInterface loginInterface;
    public LoginPresenter(LoginInterface loginInterface) {
        this.loginInterface = loginInterface;
    }

    public void login()
    {
        String username = loginInterface.getUsername();
        String password =  loginInterface.getPassword();

    }

    public boolean validateInputCredential()
    {
        String username = loginInterface.getUsername();
        String password =  loginInterface.getPassword();
        if(username ==null || username.isEmpty())
        {
            loginInterface.invalidUsername();
            return false;
        }
        if(password ==null || password.isEmpty())
        {
            loginInterface.invalidPassword();
            return false;
        }
        return true;
    }
}
