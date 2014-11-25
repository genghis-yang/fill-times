package genghis.tools.filltimes;

public class PersonInfo {
	private String uid;
	private String password;

	public String getUid() {
		return uid;
	}

	public String getPassword() {
		// FIXME must be decrypted
		return password;
	}

	public PersonInfo(String uid, String password) {
		this.uid = uid;
		this.password = password;
	}
}
