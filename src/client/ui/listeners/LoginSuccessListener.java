package client.ui.listeners;

@FunctionalInterface
public interface LoginSuccessListener {
	void onSuccess(String serverIP, String serverPort, String username);
}
