package luohuayu.MCForgeProtocol;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.spacehq.mc.protocol.packet.ingame.client.ClientPluginMessagePacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerPluginMessagePacket;
import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.event.session.ConnectedEvent;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import org.spacehq.packetlib.event.session.DisconnectingEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.PacketSentEvent;
import org.spacehq.packetlib.event.session.SessionListener;
import org.spacehq.packetlib.packet.Packet;
import org.spacehq.packetlib.packet.PacketProtocol;

import luohuayu.MCForgeProtocol.packet.ServerForgePluginMessagePacket;

public class MCForge {
	private MCForgeHandShake handshake;
	
	private HashMap<String,String> modList;
	private Session session;
	
	public MCForge(Session session,HashMap<String,String> modList) {
		this.modList=modList;
		this.session=session;
		this.handshake=new MCForgeHandShake(this,this.modList);
	}
	
	public void init() {
		this.session.addListener(new SessionListener() {
			public void packetReceived(PacketReceivedEvent e) {
				handle(e.getPacket());
			}
			public void packetSent(PacketSentEvent e){}
			public void connected(ConnectedEvent e){
				modifyHost();
			}
			public void disconnecting(DisconnectingEvent e){}
			public void disconnected(DisconnectedEvent e){}
		});
	}
	
	public void handle(Packet packet) {
		if(packet instanceof ServerForgePluginMessagePacket) {
			ServerForgePluginMessagePacket forgePacket=(ServerForgePluginMessagePacket)packet;
			this.session.callEvent(new PacketReceivedEvent(this.session,new ServerPluginMessagePacket(forgePacket.getChannel(),forgePacket.getData())));
			return;
		}
		
		if(packet instanceof ServerPluginMessagePacket) {
			ServerPluginMessagePacket mcPacket=(ServerPluginMessagePacket)packet;
			switch(mcPacket.getChannel()) {
			case "FML|HS":
				this.handshake.handle(this.session,mcPacket);
				break;
			case "REGISTER":
				this.session.send(new ClientPluginMessagePacket("REGISTER",mcPacket.getData()));
				break;
			case "MC|Brand":
				this.session.send(new ClientPluginMessagePacket("MC|Brand","fml,forge".getBytes()));
				break;
			}
			return;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void modifyPacket(Class<? extends Packet> oldPacket,Class<? extends Packet> newPacket) {
		try {
			PacketProtocol protocol=this.session.getPacketProtocol();
			Class<?> cls=protocol.getClass().getSuperclass();

			Field field=cls.getDeclaredField("incoming");
			field.setAccessible(true);

			Map<Integer, Class<? extends Packet>> incoming=(Map<Integer, Class<? extends Packet>>) field.get(protocol);;
			
			incoming.forEach((id,packet)->{
				if(packet.equals(oldPacket)) {
					incoming.replace(id,newPacket);
				}
			});
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	public void modifyHost() {
		try {
			Class<?> cls=this.session.getClass().getSuperclass();
		
			Field field=cls.getDeclaredField("host");
			field.setAccessible(true);
			
			field.set(this.session, this.session.getHost()+"\0FML\0");
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isVersion1710() {
		try {
			Class<?> cls = Class.forName("org.spacehq.mc.protocol.ProtocolConstants");
			if (cls!=null) {
				Field field=cls.getDeclaredField("PROTOCOL_VERSION");
				int protocol=field.getInt(cls.newInstance());
				return (protocol==5);
			}else{
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
}
