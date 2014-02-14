package com.RHCCServer.Model;

import java.util.ArrayList;

public class Group {
	
	private ArrayList<Client> group;
	
	public void addMember(Client client)
	{
		group.add(client);
	}
	
	public boolean removeMember(Client client)
	{
		return group.remove(client);
	}
}
