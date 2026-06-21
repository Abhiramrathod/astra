package io.astra.test;

import io.astra.api.*;
import io.astra.api.fact.Fact;
import io.astra.api.store.WorldStateStore;
import io.astra.api.cache.PlanCache;
import io.astra.api.validation.Validator;
import io.astra.api.policy.PolicyChecker;
import io.astra.api.telemetry.Tracer;
import io.astra.api.telemetry.MetricsCollector;
import io.astra.api.agentbus.AgentBus;
import io.astra.api.agentbus.AgentMessage;
import io.astra.api.skill.Skill;
import io.astra.api.approval.GoalChoiceApprover;
import io.astra.api.config.AstraConfig;
import io.astra.api.event.AstraEvent;
import io.astra.api.event.AstraEventType;
import io.astra.api.event.AstraEventListener;
import io.astra.core.DefaultAstra;
import io.astra.core.StateHistory;
import io.astra.core.cache.LruPlanCache;
import io.astra.core.policy.DefaultPolicyChecker;
import io.astra.core.policy.PolicyInterceptor;
import io.astra.core.skill.SkillManager;
import io.astra.core.composite.DefaultCompositeAgent;
import io.astra.core.approval.ConsoleGoalChoiceApprover;
import io.astra.core.scheduler.DefaultSchedulerService;
import io.astra.scanner.AgentScanner;
import io.astra.validation.DefaultValidator;
import io.astra.validation.ValidationInterceptor;
import io.astra.store.InMemoryWorldStateStore;
import io.astra.store.FileWorldStateStore;
import io.astra.agentbus.DefaultAgentBus;
import io.astra.telemetry.Slf4jTracer;
import io.astra.telemetry.SimpleMetricsCollector;
import io.astra.telemetry.TelemetryInterceptor;
import io.astra.event.DefaultEventBus;
import io.astra.interceptor.DefaultInterceptorChain;
import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FeatureTests {

    @Nested
    @Order(1)
    class TypedFactsTest {
        @Test void testFactCreation() {
            Fact<String> sf = new Fact<>("name", "astra", String.class);
            assertEquals("astra", sf.getValue());
            assertEquals(String.class, sf.getType());
        }

        @Test void testFactInteger() {
            Fact<Integer> f = new Fact<>("count", 42, Integer.class);
            assertEquals(42, f.getValue());
        }

        @Test void testFactSerialization() {
            Fact<Integer> f = Fact.deserialize("count", "42", Integer.class);
            assertEquals(42, f.getValue());
        }

        @Test void testTypedWorldState() {
            WorldState ws = WorldStates.empty();
            ws = ws.setTyped("age", 30, Integer.class);
            Optional<Integer> age = ws.getTyped("age", Integer.class);
            assertTrue(age.isPresent());
            assertEquals(30, age.get());
        }

        @Test void testTypedWorldStateTypeMismatch() {
            WorldState ws = WorldStates.empty();
            ws = ws.setTyped("val", "hello", String.class);
            Optional<Integer> val = ws.getTyped("val", Integer.class);
            assertFalse(val.isPresent());
        }

        @Test void testTypedWorldStateBackwardCompat() {
            WorldState ws = WorldStates.of("key", "value");
            assertEquals("value", ws.get("key").orElse(null));
        }
    }

    @Nested
    @Order(2)
    class WorldStateStoreTest {
        @Test void testInMemoryStore() {
            WorldStateStore store = new InMemoryWorldStateStore();
            WorldState ws = WorldStates.of("x", "1");
            store.save("test1", ws);
            assertTrue(store.exists("test1"));
            assertEquals("1", store.load("test1").orElseThrow().get("x").orElse(null));
            store.delete("test1");
            assertFalse(store.exists("test1"));
        }

        @Test void testFileStore() throws Exception {
            Path dir = Files.createTempDirectory("astra-test-");
            FileWorldStateStore store = new FileWorldStateStore(dir);
            WorldState ws = WorldStates.of("hello", "world");
            store.save("session1", ws);
            assertTrue(store.exists("session1"));
            WorldState loaded = store.load("session1").orElseThrow();
            assertEquals("world", loaded.get("hello").orElse(null));
            store.delete("session1");
            assertFalse(store.exists("session1"));
            Files.deleteIfExists(dir);
        }
    }

    @Nested
    @Order(3)
    class PlanCacheTest {
        @Test void testLruCache() {
            PlanCache cache = new LruPlanCache(2);
            WorldState s1 = WorldStates.of("a", "1");
            WorldState s2 = WorldStates.of("b", "2");
            WorldState s3 = WorldStates.of("c", "3");
            Plan p1 = emptyPlan(), p2 = emptyPlan(), p3 = emptyPlan();
            cache.put("g1", s1, p1);
            cache.put("g1", s2, p2);
            assertNotNull(cache.get("g1", s1));
            assertNotNull(cache.get("g1", s2));
            cache.put("g1", s3, p3);
            assertNull(cache.get("g1", s1));
        }

        @Test void testCacheInvalidate() {
            PlanCache cache = new LruPlanCache(10);
            cache.put("g1", WorldStates.empty(), emptyPlan());
            cache.invalidate("g1");
            assertNull(cache.get("g1", WorldStates.empty()));
        }
    }

    @Nested
    @Order(4)
    class ValidationTest {
        @Test void testValidatorPasses() {
            Validator v = new DefaultValidator();
            WorldState ws = WorldStates.of("key", "value");
            assertTrue(v.validate(ws, "testAction").isEmpty());
        }

        @Test void testValidatorFailsOnNull() {
            Validator v = new DefaultValidator();
            WorldState ws = new WorldState() {
                public Optional<String> get(String k) { return Optional.ofNullable(null); }
                public boolean matches(Map<String, String> c) { return true; }
                public Map<String, String> asMap() {
                    Map<String, String> m = new HashMap<>();
                    m.put("nullKey", null);
                    return m;
                }
                public WorldState set(String k, String v) { return this; }
                public <T> Optional<T> getTyped(String k, Class<T> t) { return Optional.empty(); }
                public <T> WorldState setTyped(String k, T v, Class<T> t) { return this; }
            };
            assertFalse(v.validate(ws, "test").isEmpty());
        }
    }

    @Nested
    @Order(5)
    class PolicyTest {
        @Test void testPolicyChecker() {
            DefaultPolicyChecker checker = new DefaultPolicyChecker();
            checker.grant("adminAction", "admin");
            assertTrue(checker.check("adminAction", "admin", WorldStates.empty()));
            assertFalse(checker.check("otherAction", "admin", WorldStates.empty()));
        }
    }

    @Nested
    @Order(6)
    class AgentBusTest {
        @Test void testSendMessage() {
            DefaultEventBus eb = new DefaultEventBus();
            DefaultAgentBus bus = new DefaultAgentBus(eb);
            List<String> received = new ArrayList<>();
            bus.subscribe("agent2", msg -> received.add(msg.getTopic()));
            bus.send(new AgentMessage("agent1", "agent2", "hello", "payload"));
            assertEquals(1, received.size());
            assertEquals("hello", received.get(0));
        }

        @Test void testBroadcast() {
            DefaultEventBus eb = new DefaultEventBus();
            DefaultAgentBus bus = new DefaultAgentBus(eb);
            AtomicInteger count = new AtomicInteger(0);
            bus.subscribe("a", msg -> count.incrementAndGet());
            bus.subscribe("b", msg -> count.incrementAndGet());
            bus.broadcast("sender", "topic", "data");
            assertEquals(2, count.get());
        }
    }

    @Nested
    @Order(7)
    class SkillManagerTest {
        @Test void testLoadUnload() {
            SkillManager mgr = new SkillManager(new io.astra.config.MapConfigProvider());
            Skill skill = new Skill() {
                public String getName() { return "test-skill"; }
                public String getVersion() { return "1.0"; }
                public void init(AstraConfig c) {}
                public void destroy() {}
            };
            mgr.load(skill);
            assertTrue(mgr.getSkill("test-skill").isPresent());
            assertEquals(1, mgr.getSkillNames().size());
            mgr.unload("test-skill");
            assertFalse(mgr.getSkill("test-skill").isPresent());
        }
    }

    @Nested
    @Order(8)
    class TelemetryTest {
        @Test void testTracer() {
            Slf4jTracer tracer = new Slf4jTracer();
            assertDoesNotThrow(() -> tracer.traceActionStart("test", WorldStates.empty()));
        }

        @Test void testMetrics() {
            SimpleMetricsCollector metrics = new SimpleMetricsCollector();
            metrics.incrementCounter("test.counter");
            metrics.recordTimer("test.timer", 100);
            assertEquals(1, metrics.getCounter("test.counter"));
        }
    }

    @Nested
    @Order(9)
    class StateHistoryTest {
        @Test void testSnapshotAndRollback() {
            DefaultEventBus eb = new DefaultEventBus();
            StateHistory history = new StateHistory(5, eb);
            WorldState ws1 = WorldStates.of("a", "1");
            WorldState ws2 = ws1.set("a", "2");
            history.snapshot(ws1);
            WorldState restored = history.rollback();
            assertEquals("1", restored.get("a").orElse(null));
            assertEquals(0, history.depth());
        }

        @Test void testRollbackEmpty() {
            DefaultEventBus eb = new DefaultEventBus();
            StateHistory history = new StateHistory(5, eb);
            assertThrows(IllegalStateException.class, history::rollback);
        }
    }

    @Nested
    @Order(10)
    class CompositeAgentTest {
        @Test void testRegisterAndPlan() {
            DefaultEventBus eb = new DefaultEventBus();
            DefaultInterceptorChain ic = new DefaultInterceptorChain();
            io.astra.config.MapConfigProvider cfg = new io.astra.config.MapConfigProvider();
            DefaultCompositeAgent composite = new DefaultCompositeAgent(eb, ic, cfg);
            assertEquals(0, composite.getSubAgentNames().size());
        }
    }

    @Nested
    @Order(11)
    class GoalChoiceApproverTest {
        @Test void testSingleCandidate() {
            GoalChoiceApprover approver = new ConsoleGoalChoiceApprover();
            GoalInfo g = new GoalInfo() {
                public String getName() { return "test"; }
                public String getDescription() { return "test goal"; }
                public Map<String, String> getCondition() { return Map.of(); }
            };
            assertEquals(g, approver.chooseGoal(List.of(g), "test"));
        }
    }

    @Nested
    @Order(12)
    class SchedulerTest {
        @Test void testScheduleOnce() throws Exception {
            DefaultEventBus eb = new DefaultEventBus();
            DefaultSchedulerService scheduler = new DefaultSchedulerService(eb);
            AtomicBoolean executed = new AtomicBoolean(false);
            var task = scheduler.schedule("test", () -> executed.set(true), Duration.ofMillis(10));
            assertNotNull(task.getId());
            Thread.sleep(50);
            assertTrue(executed.get());
            scheduler.shutdown();
        }
    }

    @Nested
    @Order(13)
    class BuilderNewFeaturesTest {
        @Test void testBuilderWithReplanning() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withReplanning(true)
                .build();
            assertNotNull(astra);
        }

        @Test void testBuilderWithReplanningLimit() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withReplanning(5)
                .build();
            assertNotNull(astra);
        }

        @Test void testBuilderWithValidator() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withValidator(new DefaultValidator())
                .build();
            assertNotNull(astra);
        }

        @Test void testBuilderWithPolicyChecker() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withPolicyChecker(new DefaultPolicyChecker())
                .build();
            assertNotNull(astra);
        }

        @Test void testBuilderWithTracer() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withTracer(new Slf4jTracer())
                .build();
            assertNotNull(astra);
        }

        @Test void testBuilderWithMetrics() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withMetrics(new SimpleMetricsCollector())
                .build();
            assertNotNull(astra);
        }

        @Test void testBuilderWithTracerAndMetrics() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withTracer(new Slf4jTracer())
                .withMetrics(new SimpleMetricsCollector())
                .build();
            assertNotNull(astra);
        }

        @Test void testBuilderWithStore() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withStore(new InMemoryWorldStateStore())
                .build();
            assertNotNull(astra);
        }

        @Test void testBuilderWithCache() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withCache(new LruPlanCache(10))
                .build();
            assertNotNull(astra);
        }

        @Test void testBuilderWithAgentBus() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withAgentBus(new DefaultAgentBus(new DefaultEventBus()))
                .build();
            assertNotNull(astra);
        }

        @Test void testBuilderWithGoalApprover() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .withGoalApprover(new ConsoleGoalChoiceApprover())
                .build();
            assertNotNull(astra);
        }
    }

    @Nested
    @Order(14)
    class SnapshotInterfaceTest {
        @Test void testSnapshotInterface() {
            WorldState ws = WorldStates.of("key", "val");
            Snapshot snap = ws.snapshot();
            WorldState restored = snap.restore();
            assertEquals("val", restored.get("key").orElse(null));
        }
    }

    @Nested
    @Order(15)
    class DefaultAstraFactoryMethodsTest {
        @Test void testCreateStore() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.createStore());
        }

        @Test void testCreateCache() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.createCache(10));
        }

        @Test void testCreateTracer() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.createTracer());
        }

        @Test void testCreateMetrics() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.createMetrics());
        }

        @Test void testCreateAgentBus() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.createAgentBus());
        }

        @Test void testCreateSkillManager() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.createSkillManager());
        }

        @Test void testCreateGoalApprover() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.createGoalApprover());
        }

        @Test void testCreateStateHistory() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.createStateHistory(10));
        }

        @Test void testCreateRepl() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.createRepl());
        }

        @Test void testCreateCompositeAgent() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.createCompositeAgent());
        }

        @Test void testGetActions() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.getActions());
        }

        @Test void testGetGoals() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.getGoals());
        }

        @Test void testGetCompoundTasks() {
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder().build();
            assertNotNull(astra.getCompoundTasks());
        }
    }

    @Nested
    @Order(16)
    class AgentBaseDxTest {
        @Test void testProgrammaticActions() {
            AgentBase agent = new AgentBase() {
                {
                    addAction("hello", () -> {});
                    addAction("greet", "Say hello", () -> {},
                        Map.of("name", "?name"), Map.of("greeted", "true"), 2.0f);
                }
            };
            assertEquals(2, agent.getDeclaredActions().size());
            ActionInfo first = agent.getDeclaredActions().get(0);
            assertEquals("hello", first.getName());
            assertEquals(1.0f, first.getCost(), 0.001);
            assertEquals(0, first.getPreconditions().size());
            assertEquals(0, first.getEffects().size());
            ActionInfo second = agent.getDeclaredActions().get(1);
            assertEquals("greet", second.getName());
            assertEquals("Say hello", second.getDescription());
            assertEquals("true", second.getEffects().get("greeted"));
            assertEquals(2.0f, second.getCost(), 0.001);
        }

        @Test void testProgrammaticGoals() {
            AgentBase agent = new AgentBase() {
                {
                    addGoal("done", Map.of("result", "ok"));
                }
            };
            assertEquals(1, agent.getDeclaredGoals().size());
            assertEquals("done", agent.getDeclaredGoals().get(0).getName());
            assertEquals("ok", agent.getDeclaredGoals().get(0).getCondition().get("result"));
        }

        @Test void testLifecycleHooks() {
            AtomicBoolean initCalled = new AtomicBoolean(false);
            AtomicBoolean destroyCalled = new AtomicBoolean(false);
            AgentBase agent = new AgentBase() {
                @Override public void onInit() { initCalled.set(true); }
                @Override public void onDestroy() { destroyCalled.set(true); }
            };
            assertFalse(initCalled.get());
            assertFalse(destroyCalled.get());
            agent.onInit();
            assertTrue(initCalled.get());
            agent.onDestroy();
            assertTrue(destroyCalled.get());
        }

        @Test void testConventionActions() {
            AgentBase agent = new AgentBase() {
                @SuppressWarnings("unused")
                public void fetchData() {}
                @SuppressWarnings("unused")
                public void processData() {}
                public String notAnAction() { return "no"; }
                private void notPublic() {}
            };
            List<ActionInfo> actions = AgentScanner.scanActions(agent);
            assertEquals(2, actions.size());
            assertTrue(actions.stream().anyMatch(a -> a.getName().equals("fetchData")));
            assertTrue(actions.stream().anyMatch(a -> a.getName().equals("processData")));
        }

        @Test void testConventionActionsExcludeObjectMethods() {
            AgentBase agent = new AgentBase() {
                @SuppressWarnings("unused")
                public void customAction() {}
            };
            List<ActionInfo> actions = AgentScanner.scanActions(agent);
            assertTrue(actions.stream().anyMatch(a -> a.getName().equals("customAction")));
            assertTrue(actions.stream().noneMatch(a -> a.getName().equals("toString")));
            assertTrue(actions.stream().noneMatch(a -> a.getName().equals("hashCode")));
            assertTrue(actions.stream().noneMatch(a -> a.getName().equals("getClass")));
        }

        @Test void testSimpleFactory() {
            AgentBase agent = new AgentBase() {
                public void doWork() {}
            };
            Astra astra = DefaultAstra.simple(agent);
            assertNotNull(astra);
            assertNotNull(astra.getConfig());
            assertNotNull(astra.getEventBus());
            assertNotNull(astra.getInterceptorChain());
        }

        @Test void testSimpleFactoryWithEffects() {
            AgentBase agent = new AgentBase() {
                {
                    addAction("work", () -> {}, Map.of(), Map.of("done", "yes"));
                }
            };
            DefaultAstra astra = (DefaultAstra) DefaultAstra.simple(agent);
            assertTrue(astra.getGoals().containsKey("_auto_goal"));
        }

        @Test void testSimpleFactoryRunAll() {
            AgentBase agent = new AgentBase() {
                {
                    addAction("step1", () -> {});
                    addAction("step2", () -> {});
                }
            };
            DefaultAstra astra = (DefaultAstra) DefaultAstra.simple(agent);
            assertTrue(astra.getGoals().containsKey("_run_all"));
        }

        @Test void testExecuteRunAll() {
            AtomicInteger counter = new AtomicInteger(0);
            AgentBase agent = new AgentBase() {
                {
                    addAction("inc1", counter::incrementAndGet);
                    addAction("inc2", counter::incrementAndGet);
                }
            };
            Astra astra = DefaultAstra.simple(agent);
            WorldState result = astra.execute("_run_all", WorldStates.empty());
            assertNotNull(result);
            assertEquals(2, counter.get());
        }

        @Test void testScanDeclaredAndConventionTogether() {
            AgentBase agent = new AgentBase() {
                {
                    addAction("declared", () -> {});
                }
                public void conventionAction() {}
            };
            List<ActionInfo> actions = AgentScanner.scanActions(agent);
            assertEquals(2, actions.size());
            assertTrue(actions.stream().anyMatch(a -> a.getName().equals("declared")));
            assertTrue(actions.stream().anyMatch(a -> a.getName().equals("conventionAction")));
        }

        @Test void testConventionActionDoesNotDuplicateDeclared() {
            AgentBase agent = new AgentBase() {
                {
                    addAction("myAction", () -> {});
                }
                public void myAction() {}
            };
            List<ActionInfo> actions = AgentScanner.scanActions(agent);
            assertEquals(1, actions.size());
        }

        @Test void testRegisterAgentBaseWithoutAnnotation() {
            AgentBase agent = new AgentBase() {
                public void doIt() {}
            };
            DefaultAstra astra = (DefaultAstra) DefaultAstra.builder()
                .register(agent)
                .build();
            assertNotNull(astra);
            assertEquals(1, astra.getActions().size());
        }

        @Test void testAutoGoalFromActionEffects() {
            List<ActionInfo> actions = List.of(
                new ActionInfo() {
                    @Override public String getName() { return "cook"; }
                    @Override public String getDescription() { return ""; }
                    @Override public Map<String, String> getPreconditions() { return Map.of(); }
                    @Override public Map<String, String> getEffects() { return Map.of("meal", "ready"); }
                    @Override public float getCost() { return 1.0f; }
                    @Override public float getUtility() { return 0.5f; }
                    @Override public Runnable getExecutor() { return () -> {}; }
                }
            );
            GoalInfo goal = AutoGoals.fromActionEffects(actions);
            assertEquals("_auto_goal", goal.getName());
            assertEquals("ready", goal.getCondition().get("meal"));
        }

        @Test void testRunAllGoal() {
            List<ActionInfo> actions = List.of(
                new ActionInfo() {
                    @Override public String getName() { return "a"; }
                    @Override public String getDescription() { return ""; }
                    @Override public Map<String, String> getPreconditions() { return Map.of(); }
                    @Override public Map<String, String> getEffects() { return Map.of(); }
                    @Override public float getCost() { return 1.0f; }
                    @Override public float getUtility() { return 0.5f; }
                    @Override public Runnable getExecutor() { return () -> {}; }
                }
            );
            GoalInfo goal = AutoGoals.runAll(actions);
            assertEquals("_run_all", goal.getName());
            assertEquals("done", goal.getCondition().get("_run_all"));
        }
    }

    private static Plan emptyPlan() {
        return new Plan() {
            @Override public List<ActionInfo> getActions() { return List.of(); }
            @Override public double getTotalCost() { return 0; }
            @Override public boolean isExecutable() { return false; }
            @Override public WorldState execute(WorldState ws) { return ws; }
        };
    }
}
