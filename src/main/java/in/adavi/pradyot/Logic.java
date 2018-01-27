package in.adavi.pradyot;

/**
 * @author Pradyot H Adavi 04/12/17
 */
public abstract class Logic<Request extends LogicRequest, Response extends LogicResponse, Context extends ExecutionContext> {

    protected boolean terminateFurtherExecution(Request request, Response response, Context context)
    {
        return false;
    }

    protected boolean shouldLogicExecute(Request request, Response response, Context context){
        return true;
    }

    protected boolean shouldFallbackLogicExecute(Request request, Response response, Context context){
        return true;
    }

    protected abstract void execute(Request request, Response response, Context context) throws LogicException;

    protected abstract void executeFallback(Request request, Response response, Context context) throws LogicException;
}
