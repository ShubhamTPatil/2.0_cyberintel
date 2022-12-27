package com.marimba.apps.securitymgr.compliance.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import com.marimba.webapps.common.Resources;
/**
 * /** VdefCarTransfer Class w.r.t fetch new dashboard data from DB
 *
 * @author inmkaklij
 *
 */

public class VdefCarTransferHandler {

	/**
	 * This method is responsible for transferring vdef.car file from one cloud Tx
	 * location to another using run channel command.
	 */
	public String transferVdefCARFile() {

		System.out.println(" Environment variable :: " + System.getenv("BCAC_HOME"));
		String responseMessage = null;
		String marimbaSetupPath = System.getenv("BCAC_HOME");
		String driveName = marimbaSetupPath.split(":")[0];
		System.out.println(" Marimba path : " + marimbaSetupPath + " Drive : " + driveName);
		try {
			Resources prop = new Resources("vdefFileTransferLocation.properties");

			String channelCopyURL = prop.getString("vdef.channel.url");
			//System.out.println(" Test key :: " + prop.getObject("vdef.channel.url"));
			String srcURL = prop.getString("vdef.source.download");
			String destURL = prop.getString("vdef.source.upload");
			System.out.println(" channelCopyURL is :: " + channelCopyURL);

			String copyChannelCommand = "runchannel " + channelCopyURL + " -src " + srcURL + " -dst " + destURL;

			System.out.println(" Command is ready :" + copyChannelCommand);

			ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/" + driveName,
					"cd \"" + "" + marimbaSetupPath + "\\Tuner\\\"" + "" + " && " + copyChannelCommand);

			builder.redirectErrorStream(true);
			Process p = builder.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while (true) {
				line = r.readLine();
				if (line == null) {
					break;
				}
				System.out.println(line);
				responseMessage = "vDef file has been transfered successfully";
			}
		} catch (Exception e) {
			responseMessage = "vDef file transfer is failed";
			e.printStackTrace();
		}
		return responseMessage;
	}
}
