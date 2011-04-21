package services;

public abstract class SigningService {
	public abstract String sign(String unformattedXml);
	
	public abstract boolean validate(String xml);
	
	public static SigningService createSigningService(String file, String password) {
		return new SigningServiceImpl(file, password);
	}
}
