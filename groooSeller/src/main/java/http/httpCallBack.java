package http;

public interface httpCallBack {

	void onSuccess(Object object);

	void onFailed(int statuscode);

}
