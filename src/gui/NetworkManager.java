package gui;

import client.Client;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import static client.Constants.CONNECTION_STATUS;
import static client.Constants.MAX_PLAYERS;

public class NetworkManager
{
    private static Connector connector;

    public static void init(Client client)
    {
        NetworkManager.connector = new Connector(client);
        NetworkManager.connector.start();
    }

    public static ReadOnlyObjectProperty<Boolean> getStatus()
    {
        return NetworkManager.connector.valueProperty();
    }

    public static ReadOnlyBooleanProperty running()
    {
        return NetworkManager.connector.runningProperty();
    }

    private static void ready(Client client)
    {
        client.send(MAX_PLAYERS);
        String response = client.receive();
        int maxPlayers = Integer.parseInt(response), currentPlayers = 0;
        while(currentPlayers < maxPlayers)
        {
            client.send(CONNECTION_STATUS);
            response = client.receive();
            currentPlayers = Integer.parseInt(response);
        }
    }

    static class Connector extends Service<Boolean>
    {
        private final Client client;

        public Connector(Client client)
        {
            this.client = client;
        }

        @Override
        protected Task<Boolean> createTask()
        {
            return new Connection(this.client);
        }
    }

    static class Connection extends Task<Boolean>
    {
        private final Client client;

        public Connection(Client client)
        {
            this.client = client;
        }

        @Override
        protected Boolean call()
        {
            updateProgress(0, 1);
            ready(this.client);
            updateProgress(1,1);
            return false;
        }
    }
}
