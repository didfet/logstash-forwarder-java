# How to create a java keystore ?

## From a .pem file

If you have your CA certificate stored in a .pem text file, run the following command to create the keystore.jks file :

		keytool -importcert -trustcacerts -file cacert.pem -alias ca -keystore keystore.jks

To list the contents of the keystore file, run this command :

		keytool -list -v -keystore keystore.jks

