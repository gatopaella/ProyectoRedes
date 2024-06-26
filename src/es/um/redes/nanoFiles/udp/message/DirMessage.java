package es.um.redes.nanoFiles.udp.message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_KEY = "key";
	private static final String FIELDNAME_USER = "user";
	private static final String FIELDNAME_ISSERVER = "isServer";
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */



	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	private String nickname;
	private int key;
	private HashMap<String, Boolean> userlist;
	
	public static final int LOGIN_FAILED_KEY = -1;


	public DirMessage(String op) {
		operation = op;
		userlist = new HashMap<String, Boolean>();
	}




	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */

	public String getOperation() {
		return operation;
	}

	public void setNickname(String nick) {



		nickname = nick;
	}

	public void setKey(int key) {
		this.key = key;
	}
	
	public void setUserlist(HashMap<String, Boolean> userlist) {
		this.userlist = new HashMap<String, Boolean>(userlist);
	}
	
	public String getNickname() {



		return nickname;
	}
	
	public int getKey() {
		return key;
	}

	public Map<String, Boolean> getUserlist() {
		return Collections.unmodifiableMap(userlist);
	}
	

	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */
		

		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;



		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();
			String user = "INVALID USER";
			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			case FIELDNAME_NICKNAME: {
				m.setNickname(value);
				break;
			}
			case FIELDNAME_KEY: {
				m.setKey(Integer.parseInt(value));
				break;
			}
			case FIELDNAME_USER: {
				user = value;
				break;
			}
			case FIELDNAME_ISSERVER: {
				m.userlist.put(user, Boolean.parseBoolean(value));
				break;
			}
			


			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}




		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: { // Si es un mensaje de login
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOGINOK: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOGOUT: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOGOUTOK: {
			break;
		}
		case DirMessageOps.OPERATION_GETUSERLIST: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_SENDUSERLIST: {
			for(String user : userlist.keySet()) {
				sb.append(FIELDNAME_USER + DELIMITER + user + END_LINE);
				sb.append(FIELDNAME_ISSERVER + DELIMITER + userlist.get(user) + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_INVALIDNICKNAME: {
			break;
		}
		case DirMessageOps.OPERATION_INVALIDKEY: {
			break;
		}

		default:
			throw new IllegalArgumentException("Unexpected value: " + operation);
		}


		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}
}
