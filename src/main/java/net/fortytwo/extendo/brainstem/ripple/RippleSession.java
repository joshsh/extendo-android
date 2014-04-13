package net.fortytwo.extendo.brainstem.ripple;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.Model;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.impl.sesame.SesameList;
import net.fortytwo.ripple.model.impl.sesame.SesameModel;
import net.fortytwo.ripple.query.LazyStackEvaluator;
import net.fortytwo.ripple.query.QueryEngine;
import net.fortytwo.ripple.query.StackEvaluator;
import net.fortytwo.ripple.sail.RippleSesameValue;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RippleSession {
    private final Sail sail;
    private final Model model;
    private final StackEvaluator evaluator;
    private final QueryEngine queryEngine;
    private final ModelConnection connection;

    private Collector<RippleList> prevCollector;
    private Collector<RippleList> curCollector;

    public RippleSession() throws RippleException {
        sail = new MemoryStore();
        try {
            sail.initialize();
        } catch (SailException e) {
            throw new RippleException(e);
        }
        model = new SesameModel(sail);
        evaluator = new LazyStackEvaluator();
        queryEngine = new QueryEngine(model, evaluator, System.out, System.err);
        connection = queryEngine.getConnection();

        prevCollector = new Collector<RippleList>();
    }

    public void close() throws RippleException {
        connection.finish();
        connection.close();
        try {
            sail.shutDown();
        } catch (SailException e) {
            throw new RippleException(e);
        }
    }

    public void push(final RippleValue... nextValues) throws RippleException {
        System.out.println("pushing new values: " + nextValues);

        if (0 == prevCollector.size()) {
            prevCollector.put(SesameList.nilList());
        }
        curCollector = new Collector<RippleList>();

        System.out.println("\tbefore evaluation:");
        for (RippleList l : prevCollector) {
            RippleList cur = l;
            for (RippleValue v : nextValues) {
                //System.out.println("pushing: " + v);
                cur = cur.push(v);
            }

            System.out.println("\t\t" + cur);

            evaluator.apply(cur, curCollector, connection);
        }

        System.out.println("\tafter evaluation:");
        for (RippleList l : curCollector) {
            System.out.println("\t\t" + l);
        }
        prevCollector = curCollector;
    }

    public ModelConnection getModelConnection() {
        return connection;
    }
}
