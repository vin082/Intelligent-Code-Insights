#!/usr/bin/env python
"""
CLI script to run batch evaluation on the mini dataset.

Usage:
    python run_evaluation.py
    python run_evaluation.py --dataset evaluation_dataset_mini.json
    python run_evaluation.py --java-path ./java_code
    python run_evaluation.py --compare-with-baseline
"""

import sys
import os
import argparse
from pathlib import Path

# Add src to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'src'))

from evaluations.batch_runner import BatchEvaluationRunner
from evaluations.analyzer import EvaluationAnalyzer


def main():
    parser = argparse.ArgumentParser(
        description='Run batch evaluation on RAG system',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Run with defaults
  python run_evaluation.py

  # Use custom dataset
  python run_evaluation.py --dataset my_dataset.json

  # Use custom Java code path
  python run_evaluation.py --java-path /path/to/java/code

  # Compare with baseline (first result in evaluation_results/)
  python run_evaluation.py --compare-with-baseline
        """
    )

    parser.add_argument(
        '--dataset',
        default='evaluation_dataset_mini.json',
        help='Path to evaluation dataset JSON (default: evaluation_dataset_mini.json)'
    )

    parser.add_argument(
        '--java-path',
        default='./java_code',
        help='Path to Java code directory (default: ./java_code)'
    )

    parser.add_argument(
        '--output-dir',
        default='evaluation_results',
        help='Directory to save results (default: evaluation_results)'
    )

    parser.add_argument(
        '--compare-with-baseline',
        action='store_true',
        help='Compare results with baseline (oldest result in output directory)'
    )

    parser.add_argument(
        '--no-save',
        action='store_true',
        help='Do not save results to file'
    )

    args = parser.parse_args()

    # Validate paths
    if not os.path.exists(args.dataset):
        print(f"❌ Error: Dataset not found: {args.dataset}")
        print(f"   Make sure the file exists or specify --dataset")
        sys.exit(1)

    if not os.path.exists(args.java_path):
        print(f"❌ Error: Java code directory not found: {args.java_path}")
        print(f"   Make sure the directory exists or specify --java-path")
        sys.exit(1)

    # Print header
    print("\n" + "=" * 70)
    print("BATCH EVALUATION RUNNER")
    print("=" * 70)
    print(f"\nDataset: {args.dataset}")
    print(f"Java Code: {args.java_path}")
    print(f"Output: {args.output_dir}")
    print()

    # Create runner
    runner = BatchEvaluationRunner(
        dataset_path=args.dataset,
        java_code_path=args.java_path
    )

    try:
        # Run batch evaluation
        batch_results = runner.run_batch()

        # Print summary
        analyzer = EvaluationAnalyzer(results_dir=args.output_dir)
        analyzer.print_summary(batch_results)

        # Save results
        if not args.no_save:
            filepath = runner.save_results(output_dir=args.output_dir)
            print(f"💾 Results saved to: {filepath}\n")

            # Compare with baseline if requested
            if args.compare_with_baseline:
                all_results = analyzer.get_all_results()

                if len(all_results) >= 2:
                    # Current is most recent, baseline is oldest
                    current = all_results[0]
                    baseline = all_results[-1]

                    print("\n" + "=" * 70)
                    print("COMPARISON WITH BASELINE")
                    print("=" * 70)

                    comparison = analyzer.compare_with_baseline(current, baseline)

                    print(f"\nBaseline: {comparison['baseline_date']}")
                    print(f"Current:  {comparison['current_date']}")
                    print(f"\nMetric Changes:")

                    for metric, delta in comparison['metric_deltas'].items():
                        direction = "↑" if delta > 0 else ("↓" if delta < 0 else "↔")
                        print(f"  {metric}: {delta:+.3f} {direction}")

                    if comparison['regressions']:
                        print(f"\n⚠️  REGRESSIONS DETECTED:")
                        for reg in comparison['regressions']:
                            print(f"  - {reg}")

                    if comparison['improvements']:
                        print(f"\n✅ IMPROVEMENTS:")
                        for imp in comparison['improvements']:
                            print(f"  - {imp}")

                    print("\n" + "=" * 70 + "\n")
                else:
                    print("ℹ️  No baseline found for comparison (need at least 2 runs)\n")

        # LangSmith link
        print("🔗 View traces in LangSmith:")
        print("   https://smith.langchain.com/o/default/projects/p/IntelligentCodeInsights")
        print("   Filter by: tags.batch_evaluation")
        print()

        print("✅ Batch evaluation complete!\n")

    except KeyboardInterrupt:
        print("\n\n⚠️  Evaluation interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Error during evaluation: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
