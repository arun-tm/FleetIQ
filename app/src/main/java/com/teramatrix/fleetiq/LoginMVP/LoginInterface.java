package com.teramatrix.fleetiq.LoginMVP;

/**
 * Created by arun.singh on 9/28/2016.
 */
interface LoginInterface {

    public String getUsername();
    public String getPassword();

    public void invalidUsername();
    public void invalidPassword();

    public void loginFail(String message);
    public void loginSuccessfull(String message);
}
