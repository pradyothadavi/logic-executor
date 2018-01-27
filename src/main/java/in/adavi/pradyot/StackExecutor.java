package in.adavi.pradyot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Stack;

/**
 * @author Pradyot H Adavi 04/12/17
 */
public class StackExecutor<Request extends LogicRequest, Response extends LogicResponse, Context extends ExecutionContext> {

    private final Logger logger = LoggerFactory.getLogger(StackExecutor.class);

    private final Stack<Logic<Request, Response, Context>> executionStack;
    private Stack<Logic<Request, Response, Context>> fallbackExecutionStack;

    public StackExecutor(Stack<Logic<Request, Response, Context>> executionStack) {
        this.executionStack = executionStack;
        this.fallbackExecutionStack = new Stack<>();
    }

    public StackExecutor(Logic<Request, Response, Context>... logics){
        this.executionStack = new Stack<>();
        this.fallbackExecutionStack = new Stack<>();
        for(Logic<Request,Response,Context> logic : Arrays.asList(logics)){
            executionStack.push(logic);
        }

    }

    public void execute(Request request, Response response, Context context) throws LogicException{
        while (!executionStack.empty())
        {
            Logic<Request, Response, Context> logic = executionStack.pop();
            try {
                if(logic.shouldLogicExecute(request,response,context))
                {
                    logger.info("Begin execution of {}",logic.getClass().getCanonicalName());
                    logic.execute(request, response, context);
                    logger.info("Completed execution of {}",logic.getClass().getCanonicalName());
                    this.fallbackExecutionStack.push(logic);
                }
                if(logic.terminateFurtherExecution(request,response,context))
                {
                    while (!executionStack.empty())
                    {
                        logic = executionStack.pop();
                        logger.info("Terminated execution of {}",logic.getClass().getCanonicalName());
                    }
                    String message = request.getClass().getCanonicalName()+" terminated further logic execution.";
                    LogicException logicException = new LogicException(message);
                    logicException.setLogicClass(logic.getClass());
                    logicException.setTerminateFurtherExecution(true);
                    throw logicException;
                }
            } catch (LogicException e){
                e.setLogicClass(logic.getClass());
                while (!this.fallbackExecutionStack.empty())
                {
                    logic = this.fallbackExecutionStack.pop();
                    if(logic.shouldFallbackLogicExecute(request,response,context))
                    {
                        logger.info("Begin fallback execution of {}",logic.getClass().getCanonicalName());
                        logic.executeFallback(request, response, context);
                        logger.info("Complete fallback execution of {}",logic.getClass().getCanonicalName());
                    }
                }
                throw e;
            }
        }
    }
}
