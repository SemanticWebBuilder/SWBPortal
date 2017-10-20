/*
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 *
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público ('open source'),
 * en virtud del cual, usted podrá usarlo en las mismas condiciones con que INFOTEC lo ha diseñado y puesto a su disposición;
 * aprender de él; distribuirlo a terceros; acceder a su código fuente y modificarlo, y combinarlo o enlazarlo con otro software,
 * todo ello de conformidad con los términos y condiciones de la LICENCIA ABIERTA AL PÚBLICO que otorga INFOTEC para la utilización
 * del SemanticWebBuilder 4.0.
 *
 * INFOTEC no otorga garantía sobre SemanticWebBuilder, de ninguna especie y naturaleza, ni implícita ni explícita,
 * siendo usted completamente responsable de la utilización que le dé y asumiendo la totalidad de los riesgos que puedan derivar
 * de la misma.
 *
 * Si usted tiene cualquier duda o comentario sobre SemanticWebBuilder, INFOTEC pone a su disposición la siguiente
 * dirección electrónica:
 *  http://www.semanticwebbuilder.org.mx
 */
package org.semanticwb.servlet.internal;

import static java.lang.management.ManagementFactory.getGarbageCollectorMXBeans;
import static java.lang.management.ManagementFactory.getMemoryPoolMXBeans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBPortal;
import org.semanticwb.SWBUtils;
import org.semanticwb.base.util.SFBase64;
import org.semanticwb.model.AdminAlert;
import org.semanticwb.model.SWBContext;
import org.semanticwb.platform.SemanticMgr;
import org.semanticwb.platform.SemanticObject;
import org.semanticwb.platform.SemanticProperty;
import org.semanticwb.portal.SWBMonitor;
import org.semanticwb.portal.monitor.SWBGCDump;
import org.semanticwb.portal.monitor.SWBMonitorBeans;
import org.semanticwb.portal.monitor.SWBMonitorData;
import org.semanticwb.portal.monitor.SWBSummary;
import org.semanticwb.portal.monitor.SWBThreadDumper;

import com.sun.management.GcInfo;

/**
 * The Class Monitor.
 * 
 * @author serch
 */
public class Monitor implements InternalServlet {

	/** The log. */
	private static Logger log = SWBUtils.getLogger(Monitor.class);

	/** The pools. */
	private static List<MemoryPoolMXBean> pools;

	/** The gcmbeans. */
	private static List<GarbageCollectorMXBean> gcmbeans;
	/** The buffer. */
	private ArrayList<SWBMonitorData> buffer;

	/** The timer. */
	private Timer timer;

	/** The max. */
	private int max = 2500;
	// private int maxgc = 50;

	/** The delays. */
	private int delays = 1000;

	/** The t. */
	private TimerTask t = null;

	/** The summary. */
	private SWBSummary summary = null;

	/** The monitorbeans. */
	private SWBMonitorBeans monitorbeans = null;

	/** The secret key. */
	private SecretKey secretKey = null;

	/** The timetaken last. */
	public static long timetakenLast = 0;

	private static Queue<Long> tiempos = new LinkedList<Long>();
	private static Queue<Long> pages = new LinkedList<Long>();
	private static Queue<Float> uso = new LinkedList<Float>();
	private static int cnt = 0;
	private static final int MAX_SIZE = 10;
	private static final int UP_LIMIT = 1;
	private static long pps = 0;
	private static int alerted_CPU = 0;
	private static int alerted_TIME = 0;
	private static int alerted_PPS = 0;
	private static boolean modeOnCPU = false;
	private static boolean modeOnTIME = false;
	private static boolean modeOnPPS = false;

	// Configurable Values...
	private static float THRESHOLD_CPU = 85.0f;
	private static long THRESHOLD_TIME = 250;
	private static long THRESHOLD_PPS = 20;
	private static boolean alertOn = false;
	private static String alertEmail = "webbuilder@infotec.com.mx";
	private static String siteName = "No Name";

	static {
		pools = getMemoryPoolMXBeans();
		gcmbeans = getGarbageCollectorMXBeans();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticwb.servlet.internal.InternalServlet#init(javax.servlet.
	 * ServletContext)
	 */
	public void init(ServletContext config) throws ServletException {
		log.event("Initializing InternalServlet Monitor...");
		try {
			monitorbeans = new SWBMonitorBeans();
		} catch (Error err) {
			err.printStackTrace();
		}
		buffer = new ArrayList<SWBMonitorData>(max);
		try {
			SemanticProperty sp = SWBPlatform.getSemanticMgr().getModel(SemanticMgr.SWBAdmin)
					.getSemanticProperty(SemanticMgr.SWBAdminURI + "/PrivateKey");
			String priv = SWBPlatform.getSemanticMgr().getModel(SemanticMgr.SWBAdmin).getModelObject()
					.getProperty(sp);
			if (null == priv) {
				org.semanticwb.SWBPlatform.getSemanticMgr().createKeyPair();
				priv = SWBPlatform.getSemanticMgr().getModel(SemanticMgr.SWBAdmin).getModelObject()
						.getProperty(sp);
			}

			if (priv != null) {
				priv = priv.substring(priv.indexOf("|") + 1);
				byte[] PKey = SFBase64.decode(priv);
				byte[] pKey = SFBase64.decode(SWBPlatform.getEnv("swbMonitor/PublicKey",
						"MIHfMIGXBgkqhkiG9w0BAwEwgYkCQQCLxCFm00uKxKmedeD9XqiJ1SZ/DoXRtdibiTIv"
								+ "Ciz2MfNzu+TnGkrgsBhTpfZN00nLopd80oPFvpBZTGIUTX2FAkBxDxzqmO0rG7TMQf4b"
								+ "q5o7lIlf0DM1qcaVvFGjCt6t/NcFcko2S//V/58sqrzcyfBQKqZr0yTyqD6J4gCL4EN/"
								+ "AgIB/wNDAAJAAwR0XE5XXl4xiTpZaF2jlLvp9YRSskMWOWPa/h3Bn+ovSpEuuMwnJ8yg"
								+ "aj5/fcFNFLj5TaIRNDqTPQgbkMUI3A=="));
				if (null != pKey) {
					java.security.spec.X509EncodedKeySpec pK = new java.security.spec.X509EncodedKeySpec(pKey);

					java.security.spec.PKCS8EncodedKeySpec PK = new java.security.spec.PKCS8EncodedKeySpec(PKey);
					KeyFactory keyFact = KeyFactory.getInstance("DiffieHellman");
					KeyAgreement ka = KeyAgreement.getInstance("DiffieHellman");
					PublicKey publicKey = keyFact.generatePublic(pK);
					PrivateKey privateKey = keyFact.generatePrivate(PK);
					ka.init(privateKey);
					ka.doPhase(publicKey, true);
					secretKey = new SecretKeySpec(ka.generateSecret(), 0, 16, "AES");
				}
			}
			AdminAlert aa = AdminAlert.ClassMgr.getAdminAlert("1", SWBContext.getAdminWebSite());
			if (null == aa) {
				aa = AdminAlert.ClassMgr.createAdminAlert("1", SWBContext.getAdminWebSite());
				aa.setAlertSistemStatus(false);
				aa.setAlertMailList("webbuilder@infotec.com.mx");
				aa.setAlertCPUTH(85.0f);
				aa.setAlertTimeTH(250);
				aa.setAlertPPSTH(50);
			}
			setAlertParameter(aa);

		} catch (java.security.GeneralSecurityException gse) {
			log.error("Security Fail:", gse);
		} catch (java.lang.NullPointerException npe) {
			log.error("No access to AdminSite, probably working in Admin Maintenance", npe);
		}
		t = new TimerTask() {

			public void run() {
				_run();
			}
		};
		timer = new Timer("Monitoring Facility", true);
		timer.schedule(t, delays, delays);

		log.event("Initializing Timer Monitor(" + max + "," + delays + "ms)...");

		if (null == summary) {
			summary = new SWBSummary();
		}
	}

	/**
	 * _run.
	 */
	private void _run() {
		if (buffer.size() == max) {
			buffer.remove(0);
		}
		SWBMonitorData data = new SWBMonitorData(monitorbeans);
		buffer.add(data);

		AdminAlert aa = AdminAlert.ClassMgr.getAdminAlert("1", SWBContext.getAdminWebSite());
		if (aa != null && alertOn != aa.isAlertSistemStatus()) {
			setAlertParameter(aa);
		}
		if (alertOn) {
			if (cnt > 29) {
				if (alerted_CPU > 0)
					alerted_CPU--;
				if (alerted_PPS > 0)
					alerted_PPS--;
				if (alerted_TIME > 0)
					alerted_TIME--;
				Vector<SWBMonitor.MonitorRecord> vec = SWBPortal.getMonitor().getMonitorRecords();
				pps = (vec.get(vec.size() - 1).getHits() - vec.get(vec.size() - 2).getHits())
						/ SWBPortal.getMonitor().getDelay();

				if (Distributor.isPageCache()) {
					if (pps < THRESHOLD_PPS) {
						Distributor.setPageCache(false);
						try {
							SWBUtils.EMAIL.sendBGEmail(alertEmail, "BACK TO NORMAL", "The site " + siteName
									+ " is back to normal operation, inAttack mode has been deactivated.");
						} catch (Exception e) {
							log.error(e);
						}
						pages.clear();
						uso.clear();
						tiempos.clear();
						System.out.println("***** Back to Normal");
					}
				}
				cnt = 0;
				pages.add(pps);
				if (pages.size() > MAX_SIZE) {
					pages.poll();
				}
				Float cpu = data.instantCPU;
				uso.add(cpu);
				if (uso.size() > MAX_SIZE) {
					uso.poll();
				}
				long ltiempos = -1;
				Vector<SWBMonitor.MonitorRecord> vmr = SWBPortal.getMonitor().getAverageMonitorRecords(10);
				if (vmr.size() > 0) {
					ltiempos = vmr.lastElement().getHitsTime();
					tiempos.add(ltiempos);
				}
				if (tiempos.size() > MAX_SIZE) {
					tiempos.poll();
				}

				Iterator<SWBMonitor.MonitorRecord> iter = vmr.iterator();
				int oCPU = 0;
				for (Float ct : uso) {
					if (ct > THRESHOLD_CPU)
						oCPU++;
				}
				int oTime = 0;
				for (Long ct : tiempos) {
					if (ct > THRESHOLD_TIME)
						oTime++;
				}
				int oPages = 0;
				for (Long ct : pages) {
					if (ct > THRESHOLD_PPS)
						oPages++;
				}

				if (UP_LIMIT < oCPU && alerted_CPU == 0) {
					try {
						SWBUtils.EMAIL.sendBGEmail(alertEmail, "ALERT HIGH CPU USAGE",
								"The site " + siteName + " is working over the " + THRESHOLD_CPU + "% usage.");
					} catch (Exception e) {
						log.error(e);
					}
					alerted_CPU = MAX_SIZE * 4;
				}
				if (UP_LIMIT < oPages && alerted_PPS == 0) {
					try {
						SWBUtils.EMAIL.sendBGEmail(alertEmail, "ALERT HIGH PAGES PER SECOND", "The site " + siteName
								+ " is delivering more than " + THRESHOLD_PPS + " pages per second.");
					} catch (Exception e) {
						log.error(e);
					}
					alerted_PPS = MAX_SIZE * 4;
				}
				if (UP_LIMIT < oTime && alerted_TIME == 0) {
					try {
						SWBUtils.EMAIL.sendBGEmail(alertEmail, "ALERT HIGH RESPONSE TIME", "The site " + siteName
								+ " is generating pages over " + THRESHOLD_TIME + "ms per page.");
					} catch (Exception e) {
						log.error(e);
					}
					alerted_TIME = MAX_SIZE * 4;
				}
				if (!Distributor.isPageCache() && ((modeOnCPU && UP_LIMIT < oCPU) || (modeOnTIME && UP_LIMIT < oTime)
						|| (modeOnPPS && UP_LIMIT < oPages))) {
					try {
						SWBUtils.EMAIL.sendBGEmail(alertEmail, "ALERT HIGH IN ATTACK",
								"The site " + siteName + " might be on attack.\n\nLast values:\n" + "CPU: " + cpu
										+ "\n P/S:" + pps + "\nTime:" + ltiempos
										+ "\n\n\nThe inAttack mode has been activated");
					} catch (Exception e) {
						log.error(e);
					}
					System.out.println("***** ALERTAR MODO ATAQUE *****");
					Distributor.setPageCache(true);
				}
			}
			cnt++;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticwb.servlet.internal.InternalServlet#doProcess(javax.servlet.http.
	 * HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * org.semanticwb.servlet.internal.DistributorParams)
	 */
	public void doProcess(HttpServletRequest request, HttpServletResponse response, DistributorParams dparams)
			throws IOException, ServletException {
		if ("/ping".equals(request.getRequestURI())) {
			response.setContentType("text/plain");
			response.getWriter().println("System alive... " + buffer.get(buffer.size()-1).upTime);
			return;
		}
		if (null == secretKey) {
			response.setContentType("text/plain");
			response.getWriter().println("Not Initializad...");
			return;
		}
		try {
			if (null != request.getParameter("cmd")) {
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);
				System.out.println("********************SK:" + SFBase64.encodeBytes(secretKey.getEncoded()));
				response.setContentType("application/octet-stream");
				CipherOutputStream out = new CipherOutputStream(response.getOutputStream(), cipher);
				ObjectOutputStream data = new ObjectOutputStream(out);
				if ("summary".equals(request.getParameter("cmd"))) {
					data.writeObject(summary.getSample());

				}
				if ("datapkg".equals(request.getParameter("cmd"))) {
					data.writeObject(buffer);

				}
				if ("deathlock".equals(request.getParameter("cmd"))) {
					data.writeObject(SWBThreadDumper.dumpDeathLock());

				}
				if ("blocked".equals(request.getParameter("cmd"))) {
					data.writeObject(SWBThreadDumper.dumpBLOCKEDThread());

				}
				if ("blockedst".equals(request.getParameter("cmd"))) {
					data.writeObject(SWBThreadDumper.dumpBLOCKEDThreadWithStackTrace());

				}
				if ("threads".equals(request.getParameter("cmd"))) {
					data.writeObject(SWBThreadDumper.dumpThread());

				}
				if ("threadsst".equals(request.getParameter("cmd"))) {
					data.writeObject(SWBThreadDumper.dumpThreadWithStackTrace());

				}
				if ("lastGC".equals(request.getParameter("cmd"))) {
					data.writeObject(SWBGCDump.getGc());

				}
				if ("lastGCVerbose".equals(request.getParameter("cmd"))) {
					data.writeObject(SWBGCDump.getVerboseGc());

				}
				if ("swbmonitor".equals(request.getParameter("cmd"))) {
					data.writeObject(SWBPortal.getMonitor().getMonitorRecords());

				}
				if ("swbavgmonitor".equals(request.getParameter("cmd"))) {
					data.writeObject(SWBPortal.getMonitor().getAverageMonitorRecords(5));

				}
				if ("clearcache".equals(request.getParameter("cmd"))) {
					clearCaches(request.getParameter("passphrase"));
				}
				if ("test".equals(request.getParameter(("cmd")))) {
					data.writeObject("AAAAAAAAAAAAAAAAA");
				}

				data.flush();
				data.close();
				out.close();
			} else {
				response.setContentType("text/plain");
				response.getWriter().println("Nothing to do...");
			}
		} catch (Exception ex) {
			log.error(ex);
		}

	}

	/**
	 * _do process.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param dparams
	 *            the dparams
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ServletException
	 *             the servlet exception
	 */
	public void _doProcess(HttpServletRequest request, HttpServletResponse response, DistributorParams dparams)
			throws IOException, ServletException {
		PrintWriter out = response.getWriter();

		out.println("<html><body>Processing...");
		if (null == summary) {
			summary = new SWBSummary();
		}
		out.println(summary.getSample().GetSumaryHTML());
		out.print("<div id=\"DeathLock\"><pre>" + SWBThreadDumper.dumpDeathLock() + "<pre></div>");
		out.print("<div id=\"ThreadDump\"><pre>" + SWBThreadDumper.dumpBLOCKEDThreadWithStackTrace() + "<pre></div>");
		out.print("<div id=\"GC\"><pre>" + SWBGCDump.getVerboseGc() + "<pre></div>");
		out.print("</body></html>");
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(buf);
		os.writeObject(buffer);
		os.close();
		out.println(buf.toString().length());
		ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()));
		try {
			out.println(((ArrayList<SWBMonitorData>) is.readObject()).size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Prints the thread info.
	 * 
	 * @param ti
	 *            the ti
	 */
	private static void printThreadInfo(ThreadInfo ti) {
		StringBuilder sb = new StringBuilder(
				"\"" + ti.getThreadName() + "\"" + " Id=" + ti.getThreadId() + " in " + ti.getThreadState());
		if (ti.getLockName() != null) {
			sb.append(" on lock=" + ti.getLockName());
		}
		if (ti.isSuspended()) {
			sb.append(" (suspended)");
		}
		if (ti.isInNative()) {
			sb.append(" (running in native)");
		}
		if (ti.getLockOwnerName() != null) {
		}
		for (StackTraceElement ste : ti.getStackTrace()) {
		}
	}

	/**
	 * Format millis.
	 * 
	 * @param ms
	 *            the ms
	 * @return the string
	 */
	private static String formatMillis(long ms) {
		return String.format("%.4fsec", ms / (double) 1000);
	}

	/**
	 * Format bytes.
	 * 
	 * @param bytes
	 *            the bytes
	 * @return the string
	 */
	private static String formatBytes(long bytes) {
		long kb = bytes;
		if (bytes > 0) {
			kb = bytes / 1024;
		}
		return kb + "K";
	}

	/**
	 * Prints the verbose gc.
	 */
	public static void printVerboseGc() {

		for (GarbageCollectorMXBean gc : gcmbeans) {
			com.sun.management.GarbageCollectorMXBean gci = (com.sun.management.GarbageCollectorMXBean) gc;
			GcInfo info = gci.getLastGcInfo();
			if (null != info) {
				printGCInfo(info);
			}
		}
		for (MemoryPoolMXBean p : pools) {
			MemoryUsage u = p.getUsage();
		}
	}

	/**
	 * Prints the gc info.
	 * 
	 * @param gci
	 *            the gci
	 * @return true, if successful
	 */
	static boolean printGCInfo(GcInfo gci) {
		// initialize GC MBean

		try {
			long startTime = gci.getStartTime();
			long endTime = gci.getEndTime();

			if (startTime == endTime) {
				return false; // no gc
			}
			Map mapBefore = gci.getMemoryUsageBeforeGc();
			Map mapAfter = gci.getMemoryUsageAfterGc();

			Set memType = mapBefore.keySet();
			Iterator it = memType.iterator();
			while (it.hasNext()) {
				String type = (String) it.next();
				MemoryUsage mu1 = (MemoryUsage) mapBefore.get(type);
			}

			memType = mapAfter.keySet();
			it = memType.iterator();
			while (it.hasNext()) {
				String type = (String) it.next();
				MemoryUsage mu2 = (MemoryUsage) mapAfter.get(type);
			}
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception exp) {
			throw new RuntimeException(exp);
		}
		return true;
	}

	/**
	 * Byte array to hex string.
	 * 
	 * @param in
	 *            the in
	 * @return the string
	 */
	public static String byteArrayToHexString(byte in[]) {

		byte ch = 0x00;
		int i = 0;

		if (in == null || in.length <= 0)
			return null;

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
		StringBuilder out = new StringBuilder(in.length * 2);

		while (i < in.length) {

			ch = (byte) (in[i] & 0xF0); // Strip off high nibble
			ch = (byte) (ch >>> 4); // shift the bits down
			ch = (byte) (ch & 0x0F); // must do this is high order bit is on!

			out.append(pseudo[(int) ch]); // convert the nibble to a String Character
			ch = (byte) (in[i] & 0x0F); // Strip off low nibble
			out.append(pseudo[(int) ch]); // convert the nibble to a String Character
			i++;
		}

		String rslt = new String(out);
		return rslt;
	}

	void clearCaches(String passphrase) {
		SemanticObject.clearCache();
		SWBPortal.getResourceMgr().getResourceCacheMgr().clearCache();
	}

	public static void setAlertParameter(AdminAlert aa) {
		if (null != aa) {
			siteName = aa.getAlertTitle() == null ? "Sitio" : aa.getAlertTitle();
			alertEmail = aa.getAlertMailList() == null ? "webbuilder@infotec.com.mx" : aa.getAlertMailList();
			alertOn = aa.isAlertSistemStatus();
			THRESHOLD_CPU = aa.getAlertCPUTH();
			THRESHOLD_TIME = aa.getAlertTimeTH();
			THRESHOLD_PPS = aa.getAlertPPSTH();
			Iterator<String> iter = aa.listAlertAttackModes();
			modeOnCPU = false;
			modeOnTIME = false;
			modeOnPPS = false;
			while (iter.hasNext()) {
				String curr = iter.next();
				if ("cpu".equals(curr))
					modeOnCPU = true;
				if ("time".equals(curr))
					modeOnTIME = true;
				if ("pps".equals(curr))
					modeOnPPS = true;
			}
		}
	}

	public static void setAlertParameters(String tsiteName, String talertEmail, boolean talertOn, float cpu, long time,
			long pps, boolean mbcpu, boolean mbtime, boolean mbpps) throws NullPointerException {
		AdminAlert aa = AdminAlert.ClassMgr.getAdminAlert("1", SWBContext.getAdminWebSite());
		if (null == aa) {
			aa = AdminAlert.ClassMgr.createAdminAlert("1", SWBContext.getAdminWebSite());
		}
		siteName = tsiteName;
		aa.setAlertTitle(tsiteName);
		alertEmail = talertEmail;
		aa.setAlertMailList(talertEmail);
		alertOn = talertOn;
		aa.setAlertSistemStatus(talertOn);
		THRESHOLD_CPU = cpu;
		aa.setAlertCPUTH(cpu);
		THRESHOLD_TIME = time;
		aa.setAlertTimeTH(time);
		THRESHOLD_PPS = pps;
		aa.setAlertPPSTH(pps);
		modeOnCPU = mbcpu;
		aa.removeAlertAttackMode("cpu");
		if (mbcpu)
			aa.addAlertAttackMode("cpu");
		modeOnTIME = mbtime;
		aa.removeAlertAttackMode("time");
		if (mbcpu)
			aa.addAlertAttackMode("time");
		modeOnPPS = mbpps;
		aa.removeAlertAttackMode("pps");
		if (mbcpu)
			aa.addAlertAttackMode("pps");
	}
}

class BasureroCtl implements Serializable {

	private static final long serialVersionUID = 33233L;
	long idx = 0;
}
