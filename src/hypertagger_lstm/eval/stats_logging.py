from eval.lstm_run import LSTMRunner
from eval.accuracy_count import AccuracyCounterFactory
from eval.confusion_matrix import ConfusionMatrix
import logging
import numpy as np

class StatsLogger(object):
    def __init__(self, session, translater, X, Y, num_tokens, weights, model, model_type):
        self.X = X
        self.Y = Y
        self.num_tokens = num_tokens
        self.session = session
        self.model = model
        self.lstm_runner = LSTMRunner(session, model_type, model, X, num_tokens)
        self.translater = translater
        self.conf_mat = ConfusionMatrix(self.translater, X, num_tokens)
        self.counter_factory = AccuracyCounterFactory(weights)
        self.betas = [0.1, 0.01, 0.001]
    
    def assignedTags(self):
        return np.argmax(self.lstm_runner.prob_dists, 2)
    
    def runLSTM(self):
        self.lstm_runner.run()
    
    def eval(self, epoch):
        self.runLSTM()
        acc = self.logAccuracy(self.counter_factory.overallAccuracyCounter(), "Overall accuracy: {:.3f}% ({}/{})")
        if epoch >= 20:
            self.betaBestEval()
        self.logAccuracy(self.counter_factory.unknownAccuracyCounter(self.X, self.num_tokens), "Accuracy on unknown predicates: {:.3f}% ({}/{})")
        self.confMatEval()
        return acc
    
    def betaBestEval(self):
        for beta in self.betas:
            self.logAccuracy(self.counter_factory.betaBestAccuracyCounter(beta), "Beta = " + str(beta) + " accuracy: {:.3f}% ({}/{})")
    
    def logAccuracy(self, counter, log_msg):
        prob_dists = self.lstm_runner.prob_dists
        counter.process(self.Y, prob_dists)
        num_correct = counter.correct_count
        num_total = counter.total_count
        accuracy = counter.accuracy()
        logging.info(log_msg.format(accuracy, num_correct, num_total))
        return accuracy
    
    def confMatEval(self):
        predictions = np.argmax(self.lstm_runner.prob_dists, 2)
        self.conf_mat.process(self.Y, predictions)
        most_wrong = self.conf_mat.mostMissed(10)
        logging.info("******************************")
        logging.info(self.lstm_runner.model_type + " Confusion Matrix")
        for tag_pair in most_wrong:
            logging.info("{}\t{}".format(tag_pair, self.conf_mat.count(tag_pair)))
            examples = self.conf_mat.getExamples(tag_pair, 2)
            for example in examples:
                logging.info(example)
        logging.info("******************************")
