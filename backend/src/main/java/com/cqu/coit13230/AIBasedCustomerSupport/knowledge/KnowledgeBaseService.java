package com.cqu.coit13230.AIBasedCustomerSupport.knowledge;

import com.cqu.coit13230.AIBasedCustomerSupport.ai.OpenAiEmbeddingGateway;
import com.cqu.coit13230.AIBasedCustomerSupport.common.NotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.common.OpenAiServiceException;

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class KnowledgeBaseService {
    private final KnowledgeBaseRepository entries;
    private final UserService users;
    private final OpenAiEmbeddingGateway embeddings;
    private final double matchThreshold;

    public KnowledgeBaseService(
            KnowledgeBaseRepository entries,
            UserService users,
            OpenAiEmbeddingGateway embeddings,
            @Value("${app.ai.openai.knowledge-match-threshold:0.55}") double matchThreshold
    ) {
        this.entries = entries;
        this.users = users;
        this.embeddings = embeddings;
        this.matchThreshold = matchThreshold;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBaseView> list() {
        return entries.findAllByOrderByCategoryAscQuestionPatternAsc().stream()
                .map(KnowledgeBaseView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<KnowledgeMatch> findBestMatch(String message) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }

        List<KnowledgeBaseEntry> activeEntries =
                entries.findByActiveTrueOrderByCategoryAscQuestionPatternAsc();
        if (activeEntries.isEmpty()) {
            return Optional.empty();
        }

        List<String> embeddingInputs = new ArrayList<>(activeEntries.size() + 1);
        embeddingInputs.add(message.trim());
        activeEntries.stream()
                .map(this::embeddingText)
                .forEach(embeddingInputs::add);

        List<List<Double>> vectors = embeddings.embed(embeddingInputs);
        if (vectors.size() != embeddingInputs.size()) {
            throw new OpenAiServiceException("OpenAI embedding result count did not match the knowledge-base request");
        }

        List<Double> queryVector = vectors.getFirst();
        KnowledgeMatch best = null;
        for (int index = 0; index < activeEntries.size(); index++) {
            double score = cosineSimilarity(queryVector, vectors.get(index + 1));
            KnowledgeMatch candidate = new KnowledgeMatch(activeEntries.get(index), score);
            if (best == null || candidate.score() > best.score()) {
                best = candidate;
            }
        }

        return best != null && best.score() >= matchThreshold
                ? Optional.of(best)
                : Optional.empty();
    }

    @Transactional
    public KnowledgeBaseView create(KnowledgeBaseRequest request) {
        User current = users.currentUser();
        KnowledgeBaseEntry entry = new KnowledgeBaseEntry();
        apply(entry, request, current);
        return KnowledgeBaseView.from(entries.save(entry));
    }

    @Transactional
    public KnowledgeBaseView update(Long id, KnowledgeBaseRequest request) {
        User current = users.currentUser();
        KnowledgeBaseEntry entry = entries.findById(id)
                .orElseThrow(() -> new NotFoundException("Knowledge base entry not found"));
        apply(entry, request, current);
        return KnowledgeBaseView.from(entries.save(entry));
    }

    @Transactional
    public void delete(Long id) {
        if (!entries.existsById(id)) {
            throw new NotFoundException("Knowledge base entry not found");
        }
        entries.deleteById(id);
    }

    private void apply(KnowledgeBaseEntry entry, KnowledgeBaseRequest request, User user) {
        entry.setQuestionPattern(request.questionPattern().trim());
        entry.setAnswerTemplate(request.answerTemplate().trim());
        entry.setCategory(request.category());
        entry.setActive(request.active() == null || request.active());
        entry.setLastUpdatedBy(user);
    }

    private String embeddingText(KnowledgeBaseEntry entry) {
        return "Question patterns: " + entry.getQuestionPattern()
                + "\nApproved answer: " + entry.getAnswerTemplate()
                + "\nCategory: " + entry.getCategory();
    }

    private double cosineSimilarity(List<Double> first, List<Double> second) {
        if (first == null || second == null || first.isEmpty() || first.size() != second.size()) {
            throw new OpenAiServiceException("OpenAI returned incompatible embedding vectors");
        }

        double dot = 0;
        double firstMagnitude = 0;
        double secondMagnitude = 0;
        for (int i = 0; i < first.size(); i++) {
            double a = first.get(i);
            double b = second.get(i);
            dot += a * b;
            firstMagnitude += a * a;
            secondMagnitude += b * b;
        }

        double denominator = Math.sqrt(firstMagnitude) * Math.sqrt(secondMagnitude);
        return denominator == 0 ? 0 : dot / denominator;
    }
}
